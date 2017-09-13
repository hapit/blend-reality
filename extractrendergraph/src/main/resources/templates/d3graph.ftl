<!DOCTYPE html>
<meta charset="utf-8">
<#noparse>
<style>
    .align {
        align-items: center;
        display: flex;
        flex-direction: column;
        justify-content: center;
    }

    :root {
        --bodyFontFamily: sans-serif;
        --bodyLineHeight: 1.5;
    }

    * {
        box-sizing: inherit;
    }

    html {
        box-sizing: border-box;
        height: 100%;
    }

    body {
        font-family: var(--bodyFontFamily);
        line-height: var(--bodyLineHeight);
        margin: 0;
        min-height: 100%;
    }

    :root {
        --chartMarginBottom: 1.5rem;

        --chartBarColor: #66F;

        --chartColumnColor: #e6edf4;

        --chartAxisColor: #e6edf4;
        --chartAxisFontSize: 0.625rem;
        --chartAxisStrokeWidth: 0.125rem;
    }

    .chart {
        margin-bottom: var(--chartMarginBottom);
    }

    .chart__bar {
        fill: var(--chartBarColor);
    }

    .chart__column {
        fill: var(--chartColumnColor);
    }

    .chart__axis {
        font-size: var(--chartAxisFontSize);
    }

    .chart__axis path,
    .chart__axis line {
        fill: none;
        stroke: var(--chartAxisColor);
        stroke-width: var(--chartAxisStrokeWidth);
    }

    .chart__axis path {
        display: none;
    }

    svg {
        height: auto;
        max-width: 100%;
        vertical-align: middle;
    }


</style>
<html lang="en">

<head>
    <meta charset="utf-8">
    <title>D3 Bar Chart</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
</head>

<body class="align">

<h2>Bar Chart</h2>
<div class="chart chart--bar-chart js-bar-chart"></div>

<button class="js-bar-chart-update">Update</button>

</body>

</html>
<script src="https://d3js.org/d3.v4.min.js"></script>
<script>
    const defaults = {
        width : 1400,
        height: 300,
        margin: {
            top   : 15,
            right : 0,
            bottom: 35,
            left  : 70
        },
        axis: true,
        axisPadding: 5,
        tickSize: 10,
        barPadding: 1,
        ease: d3.easeLinear,
        nice: true,
        type: 'rounded'
    };

    class BarChart {

        constructor (element, options) {
            Object.assign(this, defaults, options);

            this.element = element;
            this.init();
        }

        dimensions() {
            const { margin } = this;

            return [
                this.width - margin.left - margin.right,
                this.height - margin.top - margin.bottom
            ];
        }

        init() {
            const { margin, tickSize, axisPadding } = this;
            const [ innerWidth, innerHeight ] = this.dimensions();

            this.graph = d3.select(this.element);

            this.dataToggle = false;

            const svg = this.svg = this.graph
                    .append('svg')
                    .attr('width', this.width)
                    .attr('height', this.height)
                    .append('g')
                    .attr('transform', `translate(${margin.left}, ${margin.top})`);

            const scaleX = this.scaleX = d3
                    .scaleTime()
                    .range([0, innerWidth]);

            const scaleY = this.scaleY = d3
                    .scaleLinear()
                    .range([innerHeight, 0]);

            const xAxis = this.xAxis = d3.axisBottom(scaleX)
                    .ticks(5)
                    .tickPadding(8)
                    .tickSize(tickSize);

            const yAxis = this.yAxis = d3.axisLeft(scaleY)
                    .ticks(3)
                    .tickPadding(8)
                    .tickSize(tickSize);

            svg
                    .append('g')
                    .attr('class', 'chart__axis chart__axis--x')
                    .attr('transform', `translate(0, ${innerHeight + axisPadding})`)
                    .call(xAxis);

            svg
                    .append('g')
                    .attr('class', 'chart__axis chart__axis--y')
                    .attr('transform', `translate(${-axisPadding}, 0)`)
                    .call(yAxis);
        }

        toggle() {
            this.dataToggle = !this.dataToggle;
        }

        renderAxis(data, options) {
            let { svg } = this;

            svg = options.animate ? svg.transition() : svg;

            svg
                    .select('.chart__axis--x')
                    .call(this.xAxis);

            svg
                    .select('.chart__axis--y')
                    .call(this.yAxis);
        }

        renderBars(data, options) {
            const { svg, scaleX, scaleY, barPadding, type, ease } = this;
            const [ innerWidth, innerHeight ] = this.dimensions();
            const barWidth = innerWidth / data.length - barPadding;

            const column = svg
                    .selectAll('.chart__column')
                    .data(data);

            column
                    .enter()
                    .append('rect')
                    .attr('class', 'chart__column');

            (options.animate ? svg.selectAll('.chart__column').transition().ease(ease) : svg.selectAll('.chart__column'))
                    .attr('x', data => scaleX(data.frame) - barWidth / 2)
        .attr('rx', type === 'rounded' ? barWidth / 2 : 0)
                    .attr('ry', type === 'rounded' ? barWidth / 2 : 0)
                    .attr('width', barWidth)
                    .attr('height', innerHeight);

            column
                    .exit()
                    .remove();

            const bar = svg
                    .selectAll('.chart__bar')
                    .data(data);

            bar
                    .enter()
                    .append('rect')
                    .attr('class', 'chart__bar');

            (options.animate ? svg.selectAll('.chart__bar').transition().ease(ease) : svg.selectAll('.chart__bar'))
                    .attr('x', data => scaleX(data.frame) - barWidth / 2)
        .attr('y', data => scaleY(this.dataToggle?data.renderTimeMillis:data.normalizedRenderTimeMillis))
        .attr('rx', type === 'rounded' ? barWidth / 2 : 0)
                    .attr('ry', type === 'rounded' ? barWidth / 2 : 0)
                    .attr('width', barWidth)
                    .attr('height', data => innerHeight - scaleY(this.dataToggle?data.renderTimeMillis:data.normalizedRenderTimeMillis));

            bar
                    .exit()
                    .remove();
        }

        render(data, options = {}) {
            const { scaleX, scaleY } = this;
            const domainX = scaleX.domain(d3.extent(data, data => data.frame));
            const domainY = scaleY.domain([0, d3.max(data, data => this.dataToggle?data.renderTimeMillis:data.normalizedRenderTimeMillis)]);

            if (this.nice) {
                domainX.nice();
                domainY.nice();
            }

            if (this.axis) {
                this.renderAxis(data, options);
            }

            this.renderBars(data, options);
        }

        update(data, options) {
            this.render(data, {
                animate: true
            });
        }

    }
    </#noparse>

    const barChart = new BarChart('.js-bar-chart');

    var jsonData = ${jsonData};

    barChart.render(jsonData.frameInfos);

    document.querySelector('.js-bar-chart-update').addEventListener('click', () =>  {
        barChart.toggle();                         barChart.update(jsonData.frameInfos);                                   });

</script>