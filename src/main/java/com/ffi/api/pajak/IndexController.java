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

    @RequestMapping(value = "/report-pajak-pratama-bekasi-json", produces = MediaType.APPLICATION_JSON_VALUE)
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

        if (!isValidDate(date)) {
            System.err.println(LocalDateTime.now().format(DATE_TIME_FORMATER) + " " + "Invalid date format: " + date);
            rm.setSuccess(false);
            rm.setMessage("Invalid date format. Use DD-Mon-YYYY format.");
            return rm;
        }

        ref.put("startDate", date);
        ref.put("endDate", date);
        ref.put("outletCode", "0232");

        List<Map<String, Object>> list = new ArrayList<>();
        Outlet outlet = new Outlet();

        try {
            list = viewServices.generateReportPajakJson(ref);
            outlet = viewServices.getOutletDetail(ref);
            outlet.setDate(date);
            outlet.setRequestRows(start + "-" + end);
            outlet.setActualResult(list.size());

            rm.setSuccess(true);
            rm.setMessage("OK");
            rm.setOutlet(outlet);
            rm.setItem(list);
            System.out.println(LocalDateTime.now().format(DATE_TIME_FORMATER) + " " + "Success generate report pajak");
        } catch (Exception e) {
            rm.setSuccess(false);
            rm.setMessage("Failed while generate Report Pajak");
            System.err.println(LocalDateTime.now().format(DATE_TIME_FORMATER) + " " + "Failed while generate Report Pajak " + e.getMessage());
        }
        return rm;
    }
}
