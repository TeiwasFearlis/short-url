package ru.test.dataBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;
import ru.test.keyGeneration.KeyCreater;

import java.util.Date;
import java.util.List;
import java.util.Map;


public class PostgreDataBase implements DataBase {


    @Autowired
    KeyCreater keyCreater;


    @Autowired
    JdbcTemplate jdbcTemplate;


    private String getAllHeadersAsString(ServerRequest request) {
        final StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : request.headers().asHttpHeaders()
                .entrySet()) {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }
        return builder.toString();
    }


    @Override
    public String addInDB(String url) {
        String sqlCheckUrl = "SELECT key From public.url_table Where url='" + url + "'";
        List<String> list = jdbcTemplate.queryForList(sqlCheckUrl, String.class);
        if (!list.isEmpty()) {
            return "localhost:8180/go/" + list.get(0);
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
        return "localhost:8180/go/" + key;
    }


    @Override
    public String returnUrl(String key, ServerRequest request) {
        String url = "SELECT url From public.url_table Where key='" + key + "'";
        List<String> stringList = jdbcTemplate.queryForList(url, String.class);
        if (!stringList.isEmpty()) {
            Date date = new Date();
            jdbcTemplate.update("INSERT INTO public.save_redirect(url,date,headers)" +
                    " VALUES('" + stringList.get(0) + "','"+ date +"','" + getAllHeadersAsString(request) + "')");
            return stringList.get(0);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
