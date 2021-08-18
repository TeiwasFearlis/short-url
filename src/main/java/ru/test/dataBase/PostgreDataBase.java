package ru.test.dataBase;

//
//public class PostgreDataBase implements DataBase {
//
//
//    final String hostname;
//    final String schema;
//
//    @Autowired
//    JdbcTemplate jdbcTemplate;
//
//    public PostgreDataBase(String hostname, String schema) {
//        this.hostname = hostname;
//        this.schema = schema;
//    }
//
//
//    private String getAllHeadersAsString(ServerRequest request) {
//        final StringBuilder builder = new StringBuilder();
//        for (Map.Entry<String, List<String>> entry : request.headers().asHttpHeaders()
//                .entrySet()) {
//            builder.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
//        }
//        return builder.toString();
//    }
//
//
//    @NotNull
//    @Override
//    public String addInDB(String url) {
//        URL ur = null;
//        try {
//            ur = new URL(url);
//        } catch (MalformedURLException e) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
//        }
//                String searchInColumnUrl = "SELECT url From "+schema+".url_table Where url='" + url + "'";
//                List<String> stringList = jdbcTemplate.queryForList(searchInColumnUrl, String.class);
//                if (!stringList.isEmpty()) {
//                    Integer returnKey = jdbcTemplate.queryForObject
//                            ("SELECT id From "+schema+".url_table Where url='" + url + "'", Integer.class);
//                    if (returnKey != null)
//                        return hostname + Long.toString(returnKey, 36);
//                }
//                jdbcTemplate.update("INSERT INTO "+schema+".url_table(url)" +
//                        " VALUES('" + url + "')");
//                String sqlCheckUrl = "SELECT id From "+schema+".url_table Where url='" + url + "'";
//                List<Integer> list = jdbcTemplate.queryForList(sqlCheckUrl, Integer.class);
//                return hostname + Long.toString(list.get(0), 36);
//            }
//
//
//
//    @Override
//    public String returnUrl(String key, ServerRequest request) {
//        try {
//            long returnKeyInId = Long.parseLong(key, 36);
//
//            String url = "SELECT url From "+schema+".url_table Where id='" + returnKeyInId + "'";
//            List<String> stringList = jdbcTemplate.queryForList(url, String.class);
//            if (!stringList.isEmpty()) {
//                Timestamp date = new Timestamp(System.currentTimeMillis());
//                jdbcTemplate.update("INSERT INTO "+schema+".save_redirect(url,date,headers)" +
//                        " VALUES('" + stringList.get(0) + "','" + date + "','" + getAllHeadersAsString(request) + "')");
//                return stringList.get(0);
//            }
//        } catch (NumberFormatException e) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
//        }
//        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//    }
//
//
//}


