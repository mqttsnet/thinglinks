package com.mqttsnet.thinglinks.tdengine.service.impl;

import com.mqttsnet.thinglinks.tdengine.api.domain.Weather;
import com.mqttsnet.thinglinks.tdengine.mapper.WeatherMapper;
import com.mqttsnet.thinglinks.tdengine.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    @Autowired
    private WeatherMapper weatherMapper;

    private Random random = new Random(System.currentTimeMillis());
    private String[] locations = {"北京", "上海", "广州", "深圳", "天津"};

    @Override
    public int init() {
        weatherMapper.dropDB();
        weatherMapper.createDB();
        weatherMapper.createSuperTable();
        long ts = System.currentTimeMillis();
        long thirtySec = 1000 * 30;
        int count = 0;
        for (int i = 0; i < 20; i++) {
            Weather weather = new Weather(new Timestamp(ts + (thirtySec * i)), 30 * random.nextFloat(), random.nextInt(100));
            weather.setLocation(locations[random.nextInt(locations.length)]);
            weather.setGroupId(i % locations.length);
            weather.setNote("note-" + i);
            weatherMapper.createTable(weather);
            count += weatherMapper.insert(weather);
        }
        return count;
    }

    @Override
    public List<Weather> query(Long limit, Long offset) {
        return weatherMapper.select(limit, offset);
    }

    @Override
    public int save(float temperature, float humidity) {
        long ts = System.currentTimeMillis();
        long thirtySec = 1000 * 30;
        Weather weather = new Weather();
        weather.setTs(new Timestamp(ts + (thirtySec)));
        weather.setTemperature(temperature);
        weather.setHumidity(humidity);
        weather.setNote("1");
        return weatherMapper.insert(weather);
    }

    @Override
    public int count() {
        return weatherMapper.count();
    }

    @Override
    public List<String> getSubTables() {
        return weatherMapper.getSubTables();
    }

    @Override
    public List<Weather> avg() {
        return weatherMapper.avg();
    }

    @Override
    public Weather lastOne() {
        Map<String, Object> result = weatherMapper.lastOne();

        long ts = (long) result.get("ts");
        float temperature = (float) result.get("temperature");
        float humidity = (float) result.get("humidity");
        String note = (String) result.get("note");
        int groupId = (int) result.get("groupid");
        String location = (String) result.get("location");

        Weather weather = new Weather(new Timestamp(ts), temperature, humidity);
        weather.setNote(note);
        weather.setGroupId(groupId);
        weather.setLocation(location);
        return weather;
    }
}
