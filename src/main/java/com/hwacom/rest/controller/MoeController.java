package com.hwacom.rest.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.WebServiceException;

import org.apache.http.NameValuePair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwacom.rest.config.MoeConfig;
import com.hwacom.rest.dao.primary.MoeSchoolListRepository;
import com.hwacom.rest.model.entity.MoeSchoolList;
import com.hwacom.rest.utils.impl.HttpClientUtils;

/**
* MOE介接用RESTAPI
* @author AlvinLiu
* @apiNote Annotation為RestController後回傳內容會被自動轉譯成JSON格式
*/
@RestController
public class MoeController {
	private static final Logger logger = LoggerFactory.getLogger(MoeController.class);
	@Autowired
	MoeSchoolListRepository moeSchoolListRepository;
	// JodaTime Utilities
	DateTimeFormatter parser    = ISODateTimeFormat.dateTimeParser();
	DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
	DateTimeFormatter prtgFormatter = DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss");
	DateTimeFormatter jsonFormatter = DateTimeFormat.forPattern("yyyy/M/d a hh:mm:ss");
	// Constant Variables
	Integer HTTP_CONNECTION_TIME_OUT = Integer.parseInt(MoeConfig.getSetting("HTTP_CONNECTION_TIME_OUT"));
	Integer HTTP_SOCKET_TIME_OUT = Integer.parseInt(MoeConfig.getSetting("HTTP_SOCKET_TIME_OUT"));
	String PRTG_SERVER_IP = MoeConfig.getSetting("PRTG_SERVER_IP");
	String PRTG_ACCOUNT = MoeConfig.getSetting("PRTG_ACCOUNT");
	String PRTG_PWD = MoeConfig.getSetting("PRTG_PWD");
	String PRTG_API_TX_COUNTER_IDX = MoeConfig.getSetting("PRTG_API_TX_COUNTER_IDX");
	String PRTG_API_RX_COUNTER_IDX = MoeConfig.getSetting("PRTG_API_TX_COUNTER_IDX");
	String PRTG_API_UPLOAD_SPEED_IDX = MoeConfig.getSetting("PRTG_API_UPLOAD_SPEED_IDX");
	String PRTG_API_DOWNLOAD_SPEED_IDX = MoeConfig.getSetting("PRTG_API_DOWNLOAD_SPEED_IDX");
	
	/**
	 * 縣市網API campus_connection_report
	 * 各連線學校對教網之連線狀態回報,可查詢最近7天內任一時間資料
	 * @param dataTimeStr 欲查詢資料時間
	 * @return Map(JSON格式)
	 */
	@CrossOrigin(maxAge = 3600)
	@RequestMapping(value = "moe/api/v1/campus_connection_report", produces = "application/json; charset=utf-8", method = {RequestMethod.POST, RequestMethod.GET})
	public Map<String, Object> campusConnectionReport(@RequestParam(name="data_time", required=false) String dataTimeStr) {
		// 宣告container並填充內容，最後會以JSON格式回傳至前端
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		//預設資料時間為系統目前時間以抓取最新狀態資料
		DateTime dataTime = new DateTime();
		try {
			// 若參數有指定時間則將參數ISO8601 String轉換為DateTime物件
			if(! StringUtils.isEmpty(dataTimeStr)) {
				dataTime = parser.parseDateTime(dataTimeStr);
			}
			if(this.chkDateTimeInWeek(dataTime)) {
				//TODO 錯誤訊息要轉換為MOE規範的格式
				rtnMap.put("infoMsg", "Error - 參數資料內容錯誤(data_time超過7天以上)");
				return rtnMap;
			}
		} catch (Exception e) {
			//TODO 錯誤訊息要轉換為MOE規範的格式
			rtnMap.put("infoMsg", "Error - 參數資料格式錯誤(data_time不是ISO8601格式日期字串)");
			rtnMap.put("errMsg", e.toString());
			return rtnMap;
		}
		/* 
		* data回傳JSON包裝格式說明
		*{
		*
		*	"total_count":int, //回傳資料總筆數
		*	"data_time":String, //該資料所屬時間區段
		*	"list":List<Map> [{ 
		*		"school_id": 學校代號, 
		*		"status": 各學校的連線狀態(ONLINE / OFFLINE / UNMANAGED / GET_DATA_FAILED), 
		*		"offline_time": 發生離線的時間(Optional)
		*}
		*/
		Integer totalCount = 0; // jdata.total_count
		List<HashMap<String,Object>> dataListMap = new ArrayList<HashMap<String,Object>>(); // jdata.list
		// EntityRepository取得資料
		List<MoeSchoolList> schoolList = moeSchoolListRepository.findAll();
		// Service取得狀態資料
		for(MoeSchoolList school : schoolList) {
			logger.info("[ School ID - " + school.getSchoolId() + "] 連線狀態辨識開始");
			HashMap<String,Object> data = new HashMap<String,Object>();
			// 預設ping狀態為GET_DATA_FAILED
			String pingStatus = "GET_DATA_FAILED";
			try {
				// case UNMANAGED(Ping Sensor ID got lost)
				if( StringUtils.isEmpty(school.getPingSensorId()) ){
					logger.error("PRTG未納管此學校(UNMANAGED) - School ID = " + school.getSchoolId());
					data.put("school_id", school.getSchoolId());
					data.put("status", "UNMANAGED");
					dataListMap.add(data);
					totalCount++;
					continue;
				}
				// 取得該校Ping狀態(return JSON String)
				String pingData = this.getSchoolPingData(school, dataTime, 5);
				// Parse JSON String為JsonNode
				ObjectMapper mapper = new ObjectMapper();
				try {
					JsonNode jsonData = mapper.readTree(pingData);
					JsonNode pingRecords = jsonData.get("histdata");					
					for(JsonNode pingRecord : pingRecords) {
						// 取得指定時間最近5分鐘的ping紀錄,應該會取樣到4-5次ping紀錄的平均值,Coverage=0%就判定為斷線
						String coverageStr  = pingRecord.get("coverage").textValue();
						String coverageSubstr = StringUtils.split(coverageStr, " ")[0];
						Integer coverage = Integer.parseInt(coverageSubstr);
						if(coverage == 0) {
							pingStatus = "OFFLINE";						
							data.put("offline_time", formatter.withZone(DateTimeZone.UTC).print(dataTime.minusMinutes(5)));
						}else {
							// 只要coverage不是0就表示曾經有連線(但是可能連線不穩)
							pingStatus = "ONLINE";
							break;
						}
					}
				}catch(Exception e) {
					logger.error("PRTG回傳資料解析JSON物件錯誤");
					logger.error(e.toString());
				}
			}catch (Exception e){
				// case GET_DATA_FAILED:依照MOE規格對取得Ping資料失敗例外處理
				logger.error("PRTG取得Ping資料失敗");
				pingStatus = "GET_DATA_FAILED";
			}
			data.put("school_id", school.getSchoolId());
			data.put("status", pingStatus);
			dataListMap.add(data);
			totalCount++;
			logger.info("[ School ID - " + school.getSchoolId() + "] 連線狀態辨識結束，判定Ping狀態為 => " + pingStatus);
		}
		// 將資料填入container
		rtnMap.put("total_count", totalCount);
		rtnMap.put("data_time", formatter.withZone(DateTimeZone.UTC).print(dataTime));
		rtnMap.put("list", dataListMap);
		// 回傳後輸出為JSON
	    return rtnMap;
	}

	/**
	 * 縣市網API campus_traffic_report
	 * 各連線學校對教網之流量統計回報
	 * 資料內容為透過該校之對外設備之Uplink port (WAN port) traffic counter
	 * 計算五分鐘區間內之流量,可查詢最近 7 天內任一時間區間資料
	 * @param dataTimeRangeStr 欲查詢資料時間
	 * @return Map(JSON格式)
	 */
	@CrossOrigin(maxAge = 3600)
	@RequestMapping(value = "moe/api/v1/campus_traffic_report", produces = "application/json; charset=utf-8", method = {RequestMethod.POST, RequestMethod.GET})
	public Map<String, Object> campusTrafficReport(@RequestParam(name="data_time_range", required=false) String dataTimeRangeStr) {
		// 宣告container並填充內容，最後會以JSON格式回傳至前端
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		//預設資料時間為系統目前時間以抓取最新狀態資料
		DateTime sDataTime = null; // 資料起始時間
		DateTime eDataTime = null; // 資料結束時間
		Boolean isDataTimeRangeEmpty = null; // 判斷參數是否有值
		try {
			// 若參數有指定時間則將參數ISO8601 String轉換為DateTime物件
			if(! StringUtils.isEmpty(dataTimeRangeStr)) {
				// 將TimeRange解析為起始時間及結束時間 分隔符號為"/"
				String dataTimeStrs[] = StringUtils.split(dataTimeRangeStr, "/");
				sDataTime = parser.parseDateTime(dataTimeStrs[0]);
				eDataTime = parser.parseDateTime(dataTimeStrs[1]);
				isDataTimeRangeEmpty = false;
			}else {
				eDataTime = new DateTime().minuteOfDay().roundFloorCopy();
				eDataTime = eDataTime.minusMinutes(eDataTime.getMinuteOfHour() % 5);
				sDataTime = eDataTime.minusMinutes(20); // PRTG有限制最少必須抓20min區間的流量資料(Ping是最少5min)不然會回傳監控資料不足
				isDataTimeRangeEmpty = true;
			}
			if(this.chkDateTimeInWeek(sDataTime)) {
				//TODO 錯誤訊息要轉換為MOE規範的格式
				rtnMap.put("infoMsg", "Error - 參數資料內容錯誤(data_time_range超過7天以上)");
				return rtnMap;
			}
		} catch (Exception e) {
			//TODO 錯誤訊息要轉換為MOE規範的格式
			rtnMap.put("infoMsg", "Error - 參數資料格式錯誤(data_time_range不是正確的ISO8601格式日期字串)");
			rtnMap.put("errMsg", e.toString());
			return rtnMap;
		}
		/* 
		* data回傳JSON包裝格式說明
		*{
		*
		*	"total_count":int, //回傳資料總筆數
		*	"list":List<Map> [{
		*		"school_id" : 各學校的連線狀態, 
		*		"traffic" : List<Map> [{
		*			"status" : FINE(正常查詢資料) / UNMANAGED(該校未納管）/ GET_DATA_FAILED(取得資料失敗)
		*			"tx_counter_5m" : int 5分鐘區間累計上傳流量 單位Bytes (PRTG預設raw data單位是Bytes)
		*			"rx_counter_5m" : int 5分鐘區間累計下載流量 單位Bytes (PRTG預設raw data單位是Bytes)
		*			"upload_utilization" : float 上傳頻寬使用率 [ 5 分鐘累積上傳 MBytes / (該單位上傳頻寬 mbps * 60 * 5 / 8) ]
		*			"download_utilization" : float 下載頻寬使用率 [ 5 分鐘累積下載 MBytes / (該單位上傳頻寬 mbps * 60 * 5 / 8) ]
		*		}]
		*	}]
		*}
		*/
		Integer totalCount = 0; // jdata.total_count
		List<HashMap<String,Object>> dataListMap = new ArrayList<HashMap<String,Object>>(); // jdata.list
		// EntityRepository取得資料
		List<MoeSchoolList> schoolList = moeSchoolListRepository.findAll();
		// Service取得狀態資料
		for(MoeSchoolList school : schoolList) {
			logger.info("[School ID - "+school.getSchoolId()+"]流量統計開始");
			HashMap<String,Object> data = new HashMap<String,Object>();
			List<HashMap<String,Object>> trafficListMap = new ArrayList<HashMap<String,Object>>();
			List<HashMap<String, Object>> dataTimePeriods = this.getDataTimePeriods(sDataTime, eDataTime); //預計回傳traffic要填充的dataTime(數個5min)
			// 取得該校頻寬
			Long bandwidth = school.getBandwidth();
			try {
				// case1 未納管學校的例外處理 UNMANAGED(Traffic Sensor ID got lost)
				if( StringUtils.isEmpty(school.getExportTrafficSensorId()) ){
					logger.error("PRTG未納管此學校(UNMANAGED) - School ID = " + school.getSchoolId());
					data.put("school_id", school.getSchoolId());
					// 未納管學校直接填入UNMANAGED跟時間區間資訊就可以
					for(HashMap<String, Object> period : dataTimePeriods) {
						HashMap<String,Object> traffic = new HashMap<String,Object>();
						String dataTimeRange = this.getDataTimeRange((DateTime)period.get("sDataTime"),(DateTime)period.get("eDataTime"));// 強制轉型有可能轉失敗
						traffic.put("data_time_range",dataTimeRange);
						traffic.put("status", "UNMANAGED");
						trafficListMap.add(traffic);
						// 沒指定data_time_range時僅需回傳一筆有效資料即可
						if(isDataTimeRangeEmpty) {
							break;
						}
					}
					continue; // 會跳去執行finally區塊
				}
				// case2 處理納管學校查詢PRTG的traffic資料統計
				// 取得該校Traffic資料(return JSON String)區間少於20min會自動從eDataTime - 20min作為sDataTime
				String trafficData = this.getSchoolTrafficData(school, sDataTime, eDataTime);
				// Parse JSON String為JsonNode
				ObjectMapper mapper = new ObjectMapper();
				// 使用Traffic資料計算counter及utilization
				try {
					JsonNode jsonData = mapper.readTree(trafficData);
					JsonNode trafficRecords = jsonData.get("histdata"); // 將traffic資料取出供比對查詢
					// 填充所有資料區間
					for(HashMap<String, Object> period : dataTimePeriods) {
						HashMap<String,Object> traffic = new HashMap<String,Object>();
						DateTime sPeriodDataTime = (DateTime)period.get("sDataTime");
						DateTime ePeriodDataTime = (DateTime)period.get("eDataTime");
						String dataTimeRange = this.getDataTimeRange(sPeriodDataTime,ePeriodDataTime);// 強制轉型有可能轉失敗
						Boolean isTrafficMiss = true; // 是否在JSON物件中遺失Traffic資料
						for(JsonNode trafficRecord : trafficRecords) {
							String periodDataTimeStr = trafficRecord.get("datetime").textValue(); // JSON資料裡頭存放的資料時戳,traffic是往前統計5分鐘EX:13:05的時戳資料是對應13:00~13:05
							DateTime periodDataTime = jsonFormatter.parseDateTime(periodDataTimeStr);
							// 搜尋JSON物件中的Traffic資料只取出落在DataRange中的資料取樣計算
							if(periodDataTime.isAfter(sPeriodDataTime) && periodDataTime.isBefore(ePeriodDataTime)) {
								// 使用JSON資料計算回傳統計的流量資料
								Long txCounter; // int 5分鐘區間累計上傳流量 單位Bytes (PRTG預設raw data單位是Bytes)
								Long rxCounter; // int 5分鐘區間累計下載流量 單位Bytes (PRTG預設raw data單位是Bytes)
								Double uploadSpeed; // JSON資料單位為上傳速度 byte / sec
								Double downloadSpeed; // JSON資料單位為下載速度 byte / sec
								Double uploadUtilization; // float 上傳頻寬使用率 [ 5 分鐘累積上傳 MBytes / (該單位上傳頻寬 mbps * 60 * 5 / 8) ]
								Double downloadUtilization; // float 下載頻寬使用率 [ 5 分鐘累積下載 MBytes / (該單位上傳頻寬 mbps * 60 * 5 / 8) ] 
								try {
									txCounter = trafficRecord.get(this.PRTG_API_TX_COUNTER_IDX).longValue();
									rxCounter = trafficRecord.get(this.PRTG_API_RX_COUNTER_IDX).longValue();
									uploadSpeed = trafficRecord.get(this.PRTG_API_UPLOAD_SPEED_IDX).doubleValue();
									downloadSpeed = trafficRecord.get(this.PRTG_API_DOWNLOAD_SPEED_IDX).doubleValue();
									uploadUtilization = (uploadSpeed * 8) / bandwidth;
									downloadUtilization = (downloadSpeed * 8) / bandwidth;
									traffic.put("status", "FINE");
									traffic.put("tx_counter_5m", txCounter.intValue());
									traffic.put("rx_counter_5m", rxCounter.intValue());
									traffic.put("upload_utilization", uploadUtilization.floatValue());
									traffic.put("download_utilization", downloadUtilization.floatValue());
									isTrafficMiss = false;
									// 沒指定data_time_range時僅需回傳一筆有效資料即可
									if(isDataTimeRangeEmpty) {
										break;
									}
								}catch(Exception e) {
									// JSON物件中使用key找歷史流量資料時發生例外視為GET_DATA_FAILED
									traffic.put("status", "GET_DATA_FAILED");
									logger.error("PRTG回傳資料解析JSON物件錯誤");
									logger.error(e.toString());
								}
							}else {
								// JSON物件歷史流量資料不在對應時窗中,直接跳至下一筆資料檢查
								continue;
							}
							// JSON物件歷史流量資料沒有任何一筆能對應此資料時窗(有可能是PRTG掉資料)
							if(isTrafficMiss) {
								traffic.put("status", "GET_DATA_FAILED");
							}
						}
						// 不管成功失敗都填入時戳並新增至traffic資料List中
						traffic.put("data_time_range",dataTimeRange);
						trafficListMap.add(traffic);
						// 沒指定data_time_range時僅需回傳一筆有效資料即可
						if(isDataTimeRangeEmpty) {
							break;
						}
						logger.info("["+dataTimeRange+"]"+"檢查完畢, status=" + traffic.get("status"));
					}
				}catch(Exception e) {
					// case3 PRTG API存取失敗的例外處理
					for(HashMap<String, Object> period : dataTimePeriods) {
						HashMap<String,Object> traffic = new HashMap<String,Object>();
						String dataTimeRange = this.getDataTimeRange((DateTime)period.get("sDataTime"),(DateTime)period.get("eDataTime"));// 強制轉型有可能轉失敗
						traffic.put("data_time_range",dataTimeRange);
						traffic.put("status", "GET_DATA_FAILED");
						trafficListMap.add(traffic);
						// 沒指定data_time_range時僅需回傳一筆有效資料即可
						if(isDataTimeRangeEmpty) {
							break;
						}
					}
					logger.error("PRTG API回傳JSON資料解析失敗例外ALL GET_DATA_FAILED, next school");
					logger.error(e.toString());
					continue;
				}
			}catch (Exception e){
				// case GET_DATA_FAILED:依照MOE規格對取得Traffic資料失敗例外處理
				logger.error("PRTG取得Traffic資料失敗getSchoolTrafficData例外");
			}finally {
				data.put("school_id", school.getSchoolId());
				data.put("traffic", trafficListMap);
				dataListMap.add(data);
				totalCount++;
				logger.info("[School ID - "+school.getSchoolId()+"]流量統計結束");
			}	
		}
		// 將資料填入container
		rtnMap.put("total_count", totalCount);
		rtnMap.put("list", dataListMap);
		// 回傳後輸出為JSON
	    return rtnMap;
	}
	
	/**
	 * 使用學校資訊取得Ping狀態資料(指定dataTime往前抓durationMins)
	 * @param school 學校資料
	 * @param dataTime 資料時間
	 * @param durationＭins 資料時間長度分鐘(往前抓)
	 * @return Json格式資料
	 */
	private String getSchoolPingData(MoeSchoolList school, DateTime dataTime, Integer durationMins) {
		// 由dataTime決定出sDate及eDate範圍(只看指定時間前5分鐘)
		String sDate = prtgFormatter.print(dataTime.minusMinutes(durationMins));
		String eDate = prtgFormatter.print(dataTime);
		Integer avg = durationMins * 60; // 換算為秒數(取durationMins區間的平均值)
        Object [] params = new Object[]{"avg","sdate","edate","usecaption","id","username","password"};
        Object [] values = new Object[]{avg.toString(),sDate,eDate,"1",school.getPingSensorId(),this.PRTG_ACCOUNT,this.PRTG_PWD};
        List<NameValuePair> nameValuePairList = HttpClientUtils.getParams(params, values);
        String url = PRTG_SERVER_IP + "api/historicdata.json";
        String result = null;
        try {
        	result = HttpClientUtils.sendHttpsGet(url, nameValuePairList); // Json格式字串
        }catch(Exception e) {
        	logger.error("url:" + url);
        	logger.error("params:{}", params);
        	logger.error("values:{}", values);
        	logger.error(e.toString());
        	throw new WebServiceException(); 
        }

        return result;
	}
	
	/**
	 * 使用學校資訊取得Traffic歷史資料(指定dataTime起迄區間) PRTG API取流量區間值不可小於20分鐘
	 * @param school 學校資料
	 * @param sDataTime 起始資料時間
	 * @param eDataTime 結束資料時間
	 * @return Json格式資料
	 */
	private String getSchoolTrafficData(MoeSchoolList school, DateTime sDataTime, DateTime eDataTime) {
		// 判斷時窗是否小於20分鐘，如小於20分鐘則強制取回20分鐘PRTG流量資料供系統取樣
		if(sDataTime.isAfter(eDataTime.minusMinutes(20))) {
			sDataTime = eDataTime.minusMinutes(20);
		}
		// 將dataTime範圍轉換為prtg參數格式
		String sDate = prtgFormatter.print(sDataTime);
		String eDate = prtgFormatter.print(eDataTime);
		Integer avg = 0; // 列舉所有資料
		// usecaption必須設為1否則回傳的JSON資料會有可能key值重複(ObjectMapper解析時會後面覆蓋前面僅存最後一筆)
        Object [] params = new Object[]{"avg","sdate","edate","usecaption","id","username","password"};
        Object [] values = new Object[]{avg.toString(),sDate,eDate,"1",school.getExportTrafficSensorId(),this.PRTG_ACCOUNT,this.PRTG_PWD};
        List<NameValuePair> nameValuePairList = HttpClientUtils.getParams(params, values);
        String url = this.PRTG_SERVER_IP + "api/historicdata.json";
        String result = null;
        try {
        	result = HttpClientUtils.sendHttpsGet(url, nameValuePairList); // Json格式字串
        }catch(Exception e) {
        	logger.error("url:" + url);
        	logger.error("params:{}", params);
        	logger.error("values:{}", values);
        	logger.error(e.toString());
        	throw new WebServiceException(); 
        }

        return result;
	}
	
	/**
	 * 將起訖時間轉換為MOE專用格式字串
	 * @param sDataTime
	 * @param eDataTime
	 * @return String
	 */
	private String getDataTimeRange(DateTime sDataTime, DateTime eDataTime) {
		String sDataTimeStr = formatter.withZone(DateTimeZone.UTC).print(sDataTime);
		String eDataTimeStr = formatter.withZone(DateTimeZone.UTC).print(eDataTime);
		return sDataTimeStr.concat("/").concat(eDataTimeStr);
	}
	
	/**
	 * 將起訖時間分拆成5分鐘區間
	 * @param sDataTime
	 * @param eDataTime
	 * @return List<Map<String,Object>>
	 */
	private List<HashMap<String, Object>> getDataTimePeriods(DateTime sDataTime, DateTime eDataTime) {
		List<HashMap<String, Object>> periods = new ArrayList<HashMap<String,Object>>();
		while(eDataTime.isAfter(sDataTime)){
			Map<String, Object> period = new HashMap<String, Object>(); 
			period.put("sDataTime", eDataTime.minusMinutes(5));
			period.put("eDataTime", eDataTime);
			eDataTime = eDataTime.minusMinutes(5);
			periods.add((HashMap<String, Object>) period);
		}
		return periods;
	}
	
	/**
	 * 驗證資料時間早於7天為true
	 * @param dataTime
	 * @return Boolean
	 */
	private Boolean chkDateTimeInWeek(DateTime dataTime) {
		return dataTime.isBefore(new DateTime().minusDays(7));
	}
}
