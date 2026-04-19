// ==================== Common Utilities ====================

const Api = {
    async get(url, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const fullUrl = queryString ? `${url}?${queryString}` : url;
        const response = await fetch(fullUrl, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
    },

    async post(url, data = {}) {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(data)
        });
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
    }
};

// ==================== Formatting Utilities ====================

const Format = {
    date(date) {
        if (!date) return '-';
        const d = new Date(date);
        return d.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    },

    time(date) {
        if (!date) return '-';
        const d = new Date(date);
        return d.toLocaleTimeString('zh-CN', {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    },

    number(num) {
        if (num == null) return '-';
        return num.toLocaleString('zh-CN');
    },

    currency(num) {
        if (num == null) return '-';
        return '$' + num.toFixed(6);
    },

    truncate(str, maxLen = 100) {
        if (!str) return '-';
        if (str.length <= maxLen) return str;
        return str.substring(0, maxLen) + '...';
    }
};

// ==================== ECharts Helpers ====================

const Charts = {
    defaultOptions: {
        responsive: true,
        maintainAspectRatio: false
    },

    // Line chart for time series data
    lineChart(dom, data, options = {}) {
        const chart = echarts.init(dom, null, Charts.defaultOptions);
        const defaultOpts = {
            tooltip: { trigger: 'axis' },
            legend: { data: data.series.map(s => s.name) },
            grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: data.xAxis
            },
            yAxis: { type: 'value' },
            series: data.series.map(s => ({
                name: s.name,
                type: 'line',
                smooth: true,
                data: s.data,
                areaStyle: s.showArea ? { opacity: 0.3 } : undefined
            }))
        };
        chart.setOption({ ...defaultOpts, ...options });
        window.addEventListener('resize', () => chart.resize());
        return chart;
    },

    // Bar chart
    barChart(dom, data, options = {}) {
        const chart = echarts.init(dom, null, Charts.defaultOptions);
        const defaultOpts = {
            tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
            legend: { data: data.series.map(s => s.name) },
            grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
            xAxis: { type: 'category', data: data.xAxis },
            yAxis: { type: 'value' },
            series: data.series.map(s => ({
                name: s.name,
                type: 'bar',
                data: s.data,
                itemStyle: {
                    borderRadius: [4, 4, 0, 0]
                }
            }))
        };
        chart.setOption({ ...defaultOpts, ...options });
        window.addEventListener('resize', () => chart.resize());
        return chart;
    },

    // Pie chart
    pieChart(dom, data, options = {}) {
        const chart = echarts.init(dom, null, Charts.defaultOptions);
        const defaultOpts = {
            tooltip: { trigger: 'item' },
            legend: { orient: 'vertical', left: 'left' },
            series: [{
                name: data.name || 'Data',
                type: 'pie',
                radius: ['40%', '70%'],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 10,
                    borderColor: '#fff',
                    borderWidth: 2
                },
                label: { show: true, formatter: '{b}: {c} ({d}%)' },
                emphasis: {
                    label: { show: true, fontSize: 16, fontWeight: 'bold' }
                },
                data: data.data
            }]
        };
        chart.setOption({ ...defaultOpts, ...options });
        window.addEventListener('resize', () => chart.resize());
        return chart;
    },

    // Gauge chart
    gaugeChart(dom, data, options = {}) {
        const chart = echarts.init(dom, null, Charts.defaultOptions);
        const defaultOpts = {
            series: [{
                type: 'gauge',
                progress: { show: true, width: 18 },
                axisLine: { lineStyle: { width: 18 } },
                axisTick: { show: false },
                splitLine: { length: 15, lineStyle: { width: 2, color: '#999' } },
                axisLabel: { distance: 25, color: '#999', fontSize: 12 },
                anchor: { show: true, showAbove: true, size: 25, itemStyle: { borderWidth: 10 } },
                title: { show: false },
                detail: {
                    valueAnimation: true,
                    fontSize: 40,
                    offsetCenter: [0, '70%'],
                    formatter: data.formatter || '{value}'
                },
                data: [{ value: data.value, name: data.name }]
            }]
        };
        chart.setOption({ ...defaultOpts, ...options });
        window.addEventListener('resize', () => chart.resize());
        return chart;
    }
};

// ==================== Vue Helpers ====================

const VueHelpers = {
    createApp(options) {
        return Vue.createApp({
            ...options,
            mounted() {
                if (options.mounted) options.mounted.call(this);
                this.$nextTick(() => {
                    if (this.initCharts) this.initCharts();
                });
            }
        });
    }
};

// ==================== Export ====================

if (typeof window !== 'undefined') {
    window.Api = Api;
    window.Format = Format;
    window.Charts = Charts;
    window.VueHelpers = VueHelpers;
}
