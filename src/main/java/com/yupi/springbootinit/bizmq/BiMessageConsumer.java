package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.enums.StatusEnum;
import com.yupi.springbootinit.service.ChartService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class BiMessageConsumer {
    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

    private int redeliveryCount = 1;

    @SneakyThrows
    @RabbitListener(queues = {BiConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel chanenel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        //限制每个消费者同时只能处理一个任务
        chanenel.basicQos(1);
        if (StringUtils.isBlank(message)) {
            //手动拒绝
            chanenel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息为空");
        }
        Chart chart = chartService.getById(Long.parseLong(message));
        if (chart == null) {
            chanenel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表为空");
        }
        StatusEnum initStatus = chart.getStatus();
        if(initStatus==StatusEnum.wait) {
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus(StatusEnum.running);
            boolean b = chartService.updateById(updateChart);
            if (!b) {
                chanenel.basicNack(deliveryTag, false, false);
                handleChartUpdate(chart.getId(), "更新图表执行中状态失败", StatusEnum.failed);
                return;
            }
        }
        //调用AI
        String result = null;
        try {
            result = aiManager.doChat(CommonConstant.MODEL_ID, buildUserInput(chart));
        } catch (Exception e) {
            handleChartUpdate(chart.getId(), e.getMessage(), StatusEnum.retry);
            if (redeliveryCount < 4) {
                chanenel.basicNack(deliveryTag, false, true);
                log.info("重试次数：{}", redeliveryCount);
                redeliveryCount++;
                Thread.sleep(30000);
                return;
            }
            redeliveryCount = 1;
            handleChartUpdate(chart.getId(), e.getMessage(), StatusEnum.failed);
            chanenel.basicReject(deliveryTag, false);
        }
        String[] splits = result.split("【【【【");
        if (splits.length < 3) {
            chanenel.basicNack(deliveryTag, false, true);
            handleChartUpdate(chart.getId(), "AI生成错误", StatusEnum.failed);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
        }
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        updateChartResult.setStatus(StatusEnum.succeed);
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            chanenel.basicNack(deliveryTag, false, false);
            handleChartUpdate(chart.getId(), "更新图表成功状态失败", StatusEnum.failed);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新图表成功状态失败");
        }
        //手动确认
        chanenel.basicAck(deliveryTag, false);
        handleChartUpdate(chart.getId(), "", StatusEnum.succeed);
    }


    private String buildUserInput(Chart chart) {
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求:").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据:").append("\n");
        userInput.append(csvData).append("\n");
        return userInput.toString();
    }

    private void handleChartUpdate(Long chartId, String message, StatusEnum status) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus(status);
        updateChart.setExecMessage(message);
        boolean b = chartService.updateById(updateChart);
        if (!b) {
            log.error("更新图表状态失败");
        }
    }
//    private void retryChart(Long chartId) {
//        Chart updateChart = new Chart();
//        updateChart.setId(chartId);
//        updateChart.setStatus(StatusEnum.retry);
//        boolean b = chartService.updateById(updateChart);
//        if (!b) {
//            log.error("更新图表状态失败");
//        }
//    }
}
