package ru.test.dateBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.test.keyGeneration.KeyCreater;

import java.util.List;

public class PostgreDataBase implements DataBaseChecking {

    @Autowired
    KeyCreater keyCreater;


    @Autowired
    JdbcTemplate jdbcTemplate;


    @Override
    public String addInDB(String url) {
        String sqlCheckUrl = "SELECT key From public.url_table Where url='" + url + "'";
        List<String> list = jdbcTemplate.queryForList(sqlCheckUrl, String.class);
        if (!list.isEmpty()) {
            return "localhost:8180/somethingServer/" + list.get(0);
        }
        String key = keyCreater.generateKey();
        String searchInColumnKey = "SELECT key From public.url_table Where key='" + key + "'";
        List<String> stringList = jdbcTemplate.queryForList(searchInColumnKey, String.class);
        if (!stringList.isEmpty()) {
            String oldKey = stringList.get(0);
            while (oldKey.equals(key)) {
                key = keyCreater.generateKey();
            }
        }
        jdbcTemplate.update("INSERT INTO public.url_table(key,url)" +
                " VALUES('" + key + "','" + url + "')");
        return "localhost:8180/somethingServer/" + key;
    }


    @Override
    public String returnUrl(String key) {
        String url = jdbcTemplate.queryForObject("SELECT url From public.url_table Where key='" + key + "'",
                String.class);
        return url;
    }

}
