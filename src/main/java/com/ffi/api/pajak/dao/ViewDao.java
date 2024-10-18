package com.ffi.api.pajak.dao;

import com.ffi.api.pajak.model.Outlet;
import java.util.List;
import java.util.Map;

public interface ViewDao {
    public List<Map<String, Object>> generateReportPajakJson(Map<String, String> ref) throws Exception;    
    public Outlet getOutletDetail(Map<String, String> ref) throws Exception;    
}
