package com.ffi.api.pajak.dao.Impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import com.ffi.api.pajak.dao.ProcessDao;
import org.springframework.stereotype.Repository;

@Repository
public class ProcessDaoImpl implements ProcessDao {
    
    private final NamedParameterJdbcTemplate jdbcTemplate;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Autowired
    public ProcessDaoImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public String getDateTimeForLog() {
        return LocalDateTime.now().format(dateTimeFormatter) + " || api-pajak || ";
    }
    
    @Override
    public void updateVersion(Map<String, Object> params) {
        String qry = """
            MERGE INTO m_menudtl target
            USING (
                SELECT :menuId AS menu_id,
                       'APPVERSION' AS type_id,
                       :description AS description,
                       0 AS id_no,
                       REPLACE(:menuId, '_', ' ') AS aplikasi,
                       TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') AS dbase,
                       'A' AS status,
                       'APPVERSION' AS type_menu
                FROM dual
            ) source
            ON (target.type_id = source.type_id AND target.menu_id = source.menu_id)
            WHEN MATCHED THEN
                UPDATE SET
                    target.description = source.description,
                    target.dbase = source.dbase,
                    target.id_no = source.id_no,
                    target.aplikasi = source.aplikasi,
                    target.status = source.status,
                    target.type_menu = source.type_menu
            WHEN NOT MATCHED THEN
                INSERT (type_id, menu_id, description, id_no, aplikasi, dbase, status, type_menu)
                VALUES (source.type_id, source.menu_id, source.description, source.id_no, source.aplikasi, source.dbase, source.status, source.type_menu)
                     """;
        try {
            jdbcTemplate.update(qry, params);
        } catch (DataAccessException e) {
            System.out.println(getDateTimeForLog() + "updateVersion error: " + e.getMessage());
        }
    } 
}
