package org.example.consultantrag.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component("weatherTool")
public class WeatherTool {

    private final String API_KEY = "3dbdd483e9ba7d4f12423c580bcee0a5";
    private final RestTemplate restTemplate = new RestTemplate();

    @Tool("查询指定城市的实时天气信息")
    public String getWeather(@P("城市名称，例如：Beijing, San Jose") String city) {
        try {
            // 1. 地理编码：将城市名转换为经纬度
            String geoUrl = UriComponentsBuilder.fromHttpUrl("http://api.openweathermap.org/geo/1.0/direct")
                    .queryParam("q", city)
                    .queryParam("limit", 1)
                    .queryParam("appid", API_KEY)
                    .build().toUriString();

            List<Map<String, Object>> geoLocations = restTemplate.getForObject(geoUrl, List.class);
            if (geoLocations == null || geoLocations.isEmpty()) {
                return "未能识别城市坐标：" + city;
            }

            Map<String, Object> location = geoLocations.get(0);
            double lat = (double) location.get("lat");
            double lon = (double) location.get("lon");

            // 2. 调用 One Call 3.0 接口
            long currentTimestamp = Instant.now().getEpochSecond();

            String weatherUrl = UriComponentsBuilder.fromHttpUrl("https://api.openweathermap.org/data/3.0/onecall/timemachine")
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("dt", currentTimestamp)
                    .queryParam("appid", API_KEY)
                    .queryParam("units", "metric")
                    .queryParam("lang", "zh_cn")
                    .build().toUriString();

            Map<String, Object> response = restTemplate.getForObject(weatherUrl, Map.class);

            // --- 核心修改：推算当地时间 ---
            // 获取时区偏移量（秒）
            int offsetInSeconds = ((Number) response.get("timezone_offset")).intValue();

            // 计算当地时间
            ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(offsetInSeconds);
            OffsetDateTime localTime = Instant.now().atOffset(zoneOffset);

            // 格式化时间，包含星期几，方便 AI 判断工作日
            String formattedLocalTime = localTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm (EEEE)"));

            // 3. 解析天气数据
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.get("data");
            Map<String, Object> weatherData = dataList.get(0);
            List<Map<String, Object>> weatherDesc = (List<Map<String, Object>>) weatherData.get("weather");
            String description = (String) weatherDesc.get(0).get("description");

            return String.format("""
                【%s 实时参考数据】
                当地当前时间: %s
                地理坐标: %f, %f
                天气状况: %s
                实时温度: %s°C
                体感温度: %s°C
                相对湿度: %s%%
                紫外线指数(UVI): %s
                """,
                    city, formattedLocalTime, lat, lon, description,
                    weatherData.get("temp"), weatherData.get("feels_like"),
                    weatherData.get("humidity"), weatherData.get("uvi"));

        } catch (Exception e) {
            return "天气服务调用异常: " + e.getMessage();
        }
    }
}