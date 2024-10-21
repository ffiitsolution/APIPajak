package com.ffi.api.pajak.dao.Impl;

import com.ffi.api.pajak.dao.ViewDao;
import com.ffi.api.pajak.model.Outlet;
import com.ffi.api.pajak.utils.DynamicRowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ViewDaoImpl implements ViewDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ViewDaoImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    class OutletRowMapper implements RowMapper<Outlet> {
        @Override
        public Outlet mapRow(ResultSet rs, int rowNum) throws SQLException {
            Outlet outlet = new Outlet();
            outlet.setName(rs.getString("OUTLET_NAME"));
            outlet.setAddress(rs.getString("ADDRESS_1") + " " + rs.getString("ADDRESS_2"));
            outlet.setPhone(rs.getString("PHONE"));
            return outlet;
        }
    }

    @Override
    public Outlet getOutletDetail(Map<String, String> ref) throws Exception {
        String query = "SELECT * FROM M_OUTLET mo WHERE OUTLET_CODE = :outletCode ";
        Outlet outlet = jdbcTemplate.queryForObject(query, ref, new OutletRowMapper());
        return outlet;
    }

    @Override
    public List<Map<String, Object>> generateReportPajakJson(Map<String, String> ref) throws Exception {
        String query = """
            SELECT
            	A.BILL_NO,
            	A.POS_CODE,
            	TO_CHAR(A.TRANS_DATE, 'DD-Mon-YYYY') AS TRANS_DATE,
            	SUBSTR(A.BILL_TIME, 1, 2) || ':' || SUBSTR(A.BILL_TIME, 3, 2) || ':' || SUBSTR(A.BILL_TIME, 5, 2) AS BILL_TIME,
            	C.DESCRIPTION as ORDER_TYPE,
            	SUM(PENJUALAN_F_B) AS PENJUALAN_F_B,
            	SUM(DISCOUNT) AS DISCOUNT,
            	SUM(PENJUALAN_CD) AS PENJUALAN_CD,
            	SUM(DPP) AS DPP,
            	SUM(PAJAK_RESTORAN) AS PAJAK_RESTORAN,
            	SUM(PPN) AS PPN,
            	SUM(BIAYA_ANTAR) AS BIAYA_ANTAR,
            	SUM(PJK_B_ANTAR) AS PJK_B_ANTAR,
            	SUM(TOTAL_ROUNDING) AS TOTAL_ROUNDING,
            	SUM(PENJUALAN_CD) + SUM(DPP) + SUM(PAJAK_RESTORAN) + SUM(PPN) + SUM(BIAYA_ANTAR) + SUM(PJK_B_ANTAR) AS TOTAL_BEFORE_ROUNDING,
            	SUM(TOTAL_ROUNDING) + (SUM(PENJUALAN_CD) + SUM(DPP) + SUM(PAJAK_RESTORAN) + SUM(PPN) + SUM(BIAYA_ANTAR) + SUM(PJK_B_ANTAR)) AS TOTAL_SALES
            FROM
            	(
            	SELECT
            		OUTLET_CODE,
                BILL_NO,
                POS_CODE,
                TRANS_DATE,
                ORDER_TYPE,
                BILL_TIME,
                SUM(TOTAL_AMOUNT) - SUM(PENJUALAN_CD) AS PENJUALAN_F_B,
                SUM(TOTAL_DISCOUNT) AS DISCOUNT ,
                SUM(PENJUALAN_CD) AS PENJUALAN_CD ,
                SUM(TOTAL_AMOUNT)-(SUM(PENJUALAN_CD)+ SUM(TOTAL_DISCOUNT)) AS DPP,
                SUM(TOTAL_TAX) - SUM(AMT_TAX) AS PAJAK_RESTORAN,
                SUM(AMT_TAX) AS PPN,
                SUM(TOTAL_CHARGE) AS BIAYA_ANTAR,
                SUM(TOTAL_TAX_CHARGE) AS PJK_B_ANTAR,
                SUM(TOTAL_ROUNDING) AS TOTAL_ROUNDING
            	FROM
            		(
            		SELECT
            			OUTLET_CODE,
            			BILL_NO,
            			POS_CODE,
            			TRANS_DATE,
            			BILL_TIME,
            			TOTAL_AMOUNT,
            			TOTAL_DISCOUNT,
            			0 AS PENJUALAN_CD,
            			ORDER_TYPE,
            			0 AS AMT_TAX,
            			TOTAL_TAX,
            			TOTAL_CHARGE,
            			TOTAL_TAX_CHARGE,
            			TOTAL_ROUNDING
            		FROM
            			T_POS_BILL
            		WHERE
            			OUTLET_CODE = :outletCode AND
            			TRANS_DATE BETWEEN :startDate AND :endDate AND 
            			ORDER_TYPE BETWEEN '000' AND 'ZZZ'
            	UNION
            		SELECT
            			A.OUTLET_CODE,
            			A.BILL_NO,
            			A.POS_CODE,
            			A.TRANS_DATE,
            			B.BILL_TIME,
            			0 AS TOTAL_AMOUNT,
            			0 AS TOTAL_DISCOUNT,
            			A.AMOUNT AS PENJUALAN_CD,
            			B.ORDER_TYPE,
            			A.AMT_TAX,
            			0 AS TOTAL_TAX,
            			0 AS TOTAL_CHARGE,
            			0 AS TOTAL_TAX_CHARGE,
            			0 AS TOTAL_ROUNDING
            		FROM
            			T_POS_BILL_ITEM_DETAIL A
            		LEFT JOIN T_POS_BILL B ON
            			A.BILL_NO = B.BILL_NO
            			AND A.POS_CODE = B.POS_CODE
            			AND A.TRANS_DATE = B.TRANS_DATE
            		WHERE
            			A.OUTLET_CODE = :outletCode AND
            			A.TRANS_DATE BETWEEN :startDate AND :endDate AND 
            			B.ORDER_TYPE BETWEEN '000' AND 'ZZZ'
            			AND MODIFIER_GROUP_CODE IN('M20', 'M19', 'M1Q')
            )A
            	GROUP BY
            		OUTLET_CODE,
            		BILL_NO,
            		POS_CODE,
            		TRANS_DATE,
            		BILL_TIME,
            		ORDER_TYPE 
            )A
            LEFT JOIN M_OUTLET B ON A.OUTLET_CODE = B.OUTLET_CODE
            LEFT JOIN M_GLOBAL C ON A.ORDER_TYPE = C.CODE AND COND = 'ORDER_TYPE'
            GROUP BY
            	A.BILL_NO,
            	A.POS_CODE,
            	A.TRANS_DATE,
            	A.BILL_TIME,
            	C.DESCRIPTION
            ORDER BY C.DESCRIPTION ASC, A.TRANS_DATE ASC, A.POS_CODE ASC
        """;
        List<Map<String, Object>> list = null;
        try {
            list = jdbcTemplate.query(query, ref, new DynamicRowMapper());
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }
    
    @Override
    public List<Map<String, Object>> generateReportPajakJsonWithMinMaxValue(Map<String, String> ref) throws Exception {
        String query = """
            SELECT * FROM (SELECT ROWNUM AS LINE_NUMBER, top.* FROM (
            		SELECT
            	    	A.BILL_NO,
            	    	A.POS_CODE,
            	    	TO_CHAR(A.TRANS_DATE, 'DD-Mon-YYYY') AS TRANS_DATE,
            	    	SUBSTR(A.BILL_TIME, 1, 2) || ':' || SUBSTR(A.BILL_TIME, 3, 2) || ':' || SUBSTR(A.BILL_TIME, 5, 2) AS BILL_TIME,
            	    	C.DESCRIPTION as ORDER_TYPE,
            	    	SUM(PENJUALAN_F_B) AS PENJUALAN_F_B,
            	    	SUM(DISCOUNT) AS DISCOUNT,
            	    	SUM(PENJUALAN_CD) AS PENJUALAN_CD,
            	    	SUM(DPP) AS DPP,
            	    	SUM(PAJAK_RESTORAN) AS PAJAK_RESTORAN,
            	    	SUM(PPN) AS PPN,
            	    	SUM(BIAYA_ANTAR) AS BIAYA_ANTAR,
            	    	SUM(PJK_B_ANTAR) AS PJK_B_ANTAR,
            	    	SUM(TOTAL_ROUNDING) AS TOTAL_ROUNDING,
            	    	SUM(PENJUALAN_CD) + SUM(DPP) + SUM(PAJAK_RESTORAN) + SUM(PPN) + SUM(BIAYA_ANTAR) + SUM(PJK_B_ANTAR) AS TOTAL,
            	    	SUM(TOTAL_ROUNDING) + (SUM(PENJUALAN_CD) + SUM(DPP) + SUM(PAJAK_RESTORAN) + SUM(PPN) + SUM(BIAYA_ANTAR) + SUM(PJK_B_ANTAR)) AS TOTAL_SALES
            	    FROM
            	    	(
            	    	SELECT
            	    		OUTLET_CODE,
            	    		BILL_NO,
            	    		POS_CODE,
            	    		TRANS_DATE,
            	    		ORDER_TYPE,
            	    		BILL_TIME,
            	    		SUM(TOTAL_AMOUNT) - SUM(PENJUALAN_CD) AS PENJUALAN_F_B ,
            	    		SUM(TOTAL_DISCOUNT) AS DISCOUNT ,
            	    		SUM(PENJUALAN_CD) AS PENJUALAN_CD ,
            	    		SUM(TOTAL_AMOUNT)-(SUM(PENJUALAN_CD)+ SUM(TOTAL_DISCOUNT)) AS DPP,
            	    		SUM(TOTAL_TAX) - SUM(AMT_TAX) AS PAJAK_RESTORAN,
            	    		SUM(AMT_TAX) AS PPN,
            	    		SUM(TOTAL_CHARGE) AS BIAYA_ANTAR,
            	    		SUM(TOTAL_TAX_CHARGE) AS PJK_B_ANTAR,
            	    		SUM(TOTAL_ROUNDING) AS TOTAL_ROUNDING
            	    	FROM
            	    		(
            	    		SELECT
            	    			OUTLET_CODE,
            	    			BILL_NO,
            	    			POS_CODE,
            	    			TRANS_DATE,
            	    			BILL_TIME,
            	    			TOTAL_AMOUNT,
            	    			TOTAL_DISCOUNT,
            	    			0 AS PENJUALAN_CD,
            	    			ORDER_TYPE,
            	    			0 AS AMT_TAX,
            	    			TOTAL_TAX,
            	    			TOTAL_CHARGE,
            	    			TOTAL_TAX_CHARGE,
            	    			TOTAL_ROUNDING
            	    		FROM
            	    			T_POS_BILL
            	    		WHERE
            	    			OUTLET_CODE = :outletCode AND
            	    			TRANS_DATE BETWEEN :startDate AND :endDate AND 
            	    			ORDER_TYPE BETWEEN '000' AND 'ZZZ'
            	    	UNION
            	    		SELECT
            	    			A.OUTLET_CODE,
            	    			A.BILL_NO,
            	    			A.POS_CODE,
            	    			A.TRANS_DATE,
            	    			B.BILL_TIME,
            	    			0 AS TOTAL_AMOUNT,
            	    			0 AS TOTAL_DISCOUNT,
            	    			A.AMOUNT AS PENJUALAN_CD,
            	    			B.ORDER_TYPE,
            	    			A.AMT_TAX,
            	    			0 AS TOTAL_TAX,
            	    			0 AS TOTAL_CHARGE,
            	    			0 AS TOTAL_TAX_CHARGE,
            	    			0 AS TOTAL_ROUNDING
            	    		FROM
            	    			T_POS_BILL_ITEM_DETAIL A
            	    		LEFT JOIN T_POS_BILL B ON
            	    			A.BILL_NO = B.BILL_NO
            	    			AND A.POS_CODE = B.POS_CODE
            	    			AND A.TRANS_DATE = B.TRANS_DATE
            	    		WHERE
            	    			A.OUTLET_CODE = :outletCode AND
            	    			A.TRANS_DATE BETWEEN :startDate AND :endDate AND 
            	    			B.ORDER_TYPE BETWEEN '000' AND 'ZZZ'
            	    			AND MODIFIER_GROUP_CODE IN('M20', 'M19', 'M1Q')
            	    )A
            	    	GROUP BY
            	    		OUTLET_CODE,
            	    		BILL_NO,
            	    		POS_CODE,
            	    		TRANS_DATE,
            	    		BILL_TIME,
            	    		ORDER_TYPE 
            	    )A
            	    LEFT JOIN M_OUTLET B ON A.OUTLET_CODE = B.OUTLET_CODE
            	    LEFT JOIN M_GLOBAL C ON A.ORDER_TYPE = C.CODE AND COND = 'ORDER_TYPE'
            	    GROUP BY
            	    	A.BILL_NO,
            	    	A.POS_CODE,
            	    	A.TRANS_DATE,
            	    	A.BILL_TIME,
            	    	C.DESCRIPTION
            	    ORDER BY C.DESCRIPTION ASC, A.TRANS_DATE ASC, A.POS_CODE ASC
                ) top
            ) final_result WHERE final_result.line_number BETWEEN :startNumber AND :endNumber
        """;
        List<Map<String, Object>> list = null;
        try {
            list = jdbcTemplate.query(query, ref, new DynamicRowMapper());
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }
}
