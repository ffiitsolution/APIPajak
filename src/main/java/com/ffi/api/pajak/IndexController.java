package com.ffi.api.pajak;

import com.ffi.api.pajak.model.Outlet;
import com.ffi.api.pajak.model.ResponseMessage;
import com.ffi.api.pajak.services.viewServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @Autowired
    viewServices viewServices;

    DateTimeFormatter DATE_TIME_FORMATER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DATE_PATTERN = "dd-MMM-yyyy";
    private static final Pattern DATE_REGEX = Pattern.compile("^(0[1-9]|[12][0-9]|3[01])-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-\\d{4}$");

    private static int MIN_RANGE_VALUE = 1;
    private static int MAX_RANGE_VALUE = 1000;
    
    @RequestMapping(value = "/version")
    public @ResponseBody
    Map<String, Object> tes() {
        Map<String, Object> map = new HashMap<>();
        map.put("VERSION", "ITD FFI 24.10.002");
        return map;
    }

    boolean isValidDate(String date) {
        // Validate the format using a regex
        if (date != null && !DATE_REGEX.matcher(date).matches()) {
            return false;
        }
        // Validate that the date is a proper calendar date
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
            sdf.setLenient(false);
            try {
                sdf.parse(date);
            } catch (ParseException e) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidRange(int start, int end) {
        try {
            int startNumber = start;
            int endNumber = end;
            if (endNumber < startNumber) {
                return false;
            }
            return (startNumber >= MIN_RANGE_VALUE && startNumber <= MAX_RANGE_VALUE) && (endNumber >= MIN_RANGE_VALUE && endNumber <= MAX_RANGE_VALUE);
        } catch (NumberFormatException e) {
            System.err.println(LocalDateTime.now().format(DATE_TIME_FORMATER) + " " +e.getMessage());
            return false;
        }
    }

    @GetMapping(value = "/list-trans.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Endpoint for return result report pajak Pratama Bekasi Store in JSON format", response = Object.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "The resource not found")
    })
    public @ResponseBody
    ResponseMessage generateReportPajakJson(
            @RequestParam(name = "date", required = false) String date,
            @RequestParam(name = "start", required = false) String start,
            @RequestParam(name = "end", required = false) String end
    ) throws IOException, Exception {
        Gson gsn = new Gson();
        Map<String, String> ref = gsn.fromJson("{ }", new TypeToken<Map<String, String>>() {
        }.getType());

        ResponseMessage rm = new ResponseMessage();
        if (date == null || start == null || end == null) {
            System.err.println(LocalDateTime.now().format(DATE_TIME_FORMATER) + " " + "Failed getting date value. Either date, start, end is null");
            rm.setSuccess(false);
            rm.setMessage("Failed getting completed value. date, start, end are required");
            return rm;
        }
        
        int startNumber = 0;
        int endNumber = 0;
        
        try {
            startNumber = Integer.parseInt(start);
            endNumber = Integer.parseInt(end);
        } catch (NumberFormatException e) {
            System.err.println(LocalDateTime.now().format(DATE_TIME_FORMATER) + " " + "Invalid range format: " + start + ", " + end);
            rm.setSuccess(false);
            rm.setMessage("Invalid start or end format. Only accepted number value");
            return rm;
        }

        if (!isValidRange(startNumber, endNumber)) {
            System.err.println(LocalDateTime.now().format(DATE_TIME_FORMATER) + " " + "Invalid range format: " + start + ", " + end);
            rm.setSuccess(false);
            rm.setMessage("Invalid start or end format. Value should be in 1 - 1000 range.");
            return rm;
        }

        if (!isValidDate(date)) {
            System.err.println(LocalDateTime.now().format(DATE_TIME_FORMATER) + " " + "Invalid date format: " + date);
            rm.setSuccess(false);
            rm.setMessage("Invalid date format. Use DD-Mon-YYYY format.");
            return rm;
        }

        ref.put("startDate", date);
        ref.put("endDate", date);
        ref.put("outletCode", "0232");
        ref.put("startNumber", start);
        ref.put("endNumber", end);

        List<Map<String, Object>> listAll = new ArrayList<>();
        List<Map<String, Object>> listAfterFilter = new ArrayList<>();
        Outlet outlet = new Outlet();

        try {
            listAll = viewServices.generateReportPajakJson(ref);
            listAfterFilter = viewServices.generateReportPajakJsonWithMinMaxValue(ref);
            outlet = viewServices.getOutletDetail(ref);
            
            outlet.setDate(date);
            outlet.setRequestRows(startNumber + "-" + endNumber);
            outlet.setActualResult(listAll.size());

            rm.setSuccess(true);
            rm.setMessage("OK");
            rm.setOutlet(outlet);
            rm.setItem(listAfterFilter);
            System.out.println(LocalDateTime.now().format(DATE_TIME_FORMATER) + " " + "Success generate report pajak json with param: " + " " + date + " " + startNumber + "-" + endNumber );
        } catch (Exception e) {
            rm.setSuccess(false);
            rm.setMessage("Failed while generate Report Pajak");
            System.err.println(LocalDateTime.now().format(DATE_TIME_FORMATER) + " " + "Failed while generate Report Pajak " + e.getMessage());
        }
        return rm;
    }
}
