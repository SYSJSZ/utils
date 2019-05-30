package com.sy.portal.attractInvestmentActivity.subsidyRedPacketReceiveRecord.util;

import com.google.common.collect.Maps;
import com.sy.common.util.GsonUtils;
import com.sy.common.util.HttpClientUtils;
import com.sy.common.util.JsonUtils;
import com.sy.common.util.StringUtils;
import org.json.JSONObject;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @Description:
 * @Author: lsj
 * @Date: 2019-03-01 17:14
 */
public class UtilTest {

    @Test
    public void testDate() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date scheduleActiveDate = HolidayUtils.getScheduleActiveDate(new Date(), 3);
        System.out.println(sdf.format(scheduleActiveDate));

    }
}
