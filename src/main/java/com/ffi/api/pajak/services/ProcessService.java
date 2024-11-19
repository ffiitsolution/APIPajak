package com.ffi.api.pajak.services;

import com.ffi.api.pajak.dao.ProcessDao;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessService {
    
    @Autowired
    ProcessDao dao;
    
    public void updateVersion(Map<String, Object> balance) {
        dao.updateVersion(balance);
    }
}
