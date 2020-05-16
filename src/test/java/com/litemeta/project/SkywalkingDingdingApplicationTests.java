package com.litemeta.project;

import com.alibaba.fastjson.JSON;
import com.litemeta.project.dto.AlarmMessageDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = SkywalkingDingdingApplication.class)
@AutoConfigureMockMvc
public class SkywalkingDingdingApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void paramTest() {
        AlarmMessageDto param = new AlarmMessageDto();
        param.setAlarmMessage("skywalking-test");
        param.setStartTime(System.currentTimeMillis());
        List<AlarmMessageDto> data = new ArrayList<>();
        data.add(param);
        try {
            MvcResult result = mockMvc.perform(
                    post("/alarm/pushData").contentType(MediaType.APPLICATION_JSON_UTF8).content(JSON.toJSONString(data)))
                    .andReturn();
            System.out.println(result.getResponse().getContentAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
