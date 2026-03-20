<template>
  <div>
    <h2>农户仪表盘</h2>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <div class="stat-title">今日订单</div>
          <div class="stat-value">5</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div class="stat-title">累计销售额</div>
          <div class="stat-value">¥12,345</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div class="stat-title">待发货订单</div>
          <div class="stat-value">2</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top: 20px;">
       <div id="sales-chart" style="width: 100%; height: 400px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import * as echarts from 'echarts';

onMounted(() => {
  const chartDom = document.getElementById('sales-chart');
  const myChart = echarts.init(chartDom);
  const option = {
    title: { text: '近7日销售额' },
    tooltip: { trigger: 'axis' },
    xAxis: { data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
    yAxis: {},
    series: [{ name: '销售额', type: 'line', data: [120, 200, 150, 80, 70, 110, 130] }]
  };
  myChart.setOption(option);
});
</script>

<style scoped>
.stat-title { font-size: 16px; color: #666; }
.stat-value { font-size: 24px; font-weight: bold; margin-top: 10px; }
</style>
