package com.ffi.api.pajak.services;

import com.ffi.api.pajak.dao.ViewDao;
import com.ffi.api.pajak.model.Outlet;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class viewServices {

    @Autowired
    ViewDao viewDao;
    
    public List<Map<String, Object>> generateReportPajakJson(Map<String, String> ref) throws Exception {
        return viewDao.generateReportPajakJson(ref);
    }
    public List<Map<String, Object>> generateReportPajakJsonWithMinMaxValue(Map<String, String> ref) throws Exception {
        return viewDao.generateReportPajakJsonWithMinMaxValue(ref);
    }
    public Outlet getOutletDetail(Map<String, String> ref) throws Exception {
        return viewDao.getOutletDetail(ref);
    }
    
}
