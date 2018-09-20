package tech.pinto.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.graphics2d.svg.FontMapper;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import tech.pinto.Table;

public class Chart {
	
	public static String lineChart(Table table, String id, String title, String dateFormat,
			String numberFormat, int width, int height, List<Color> colors) {
		TableDataSet data = new TableDataSet(table);
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				title.equals("") ? null : "           " + title, // title
				null, // x-axis label
				null, // y-axis label
				data, // data
				true,
				true, // generate tooltips?
				false // generate URLs?
		);
		if(!title.equals("")) {
			chart.getTitle().setFont(new Font("SansSerif", Font.ITALIC + Font.BOLD, 14));
			chart.getTitle().setVerticalAlignment(VerticalAlignment.BOTTOM);
			chart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
			chart.getTitle().setPaint(new Color(216-50, 221-50, 225-50));
			chart.getTitle().setMargin(0, 65, 0, 0);
		}


		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setBackgroundPaint(Color.decode("#D0ECEC"));
		plot.setDomainGridlinePaint(new Color(216, 221, 225));
		plot.setRangeGridlinePaint(new Color(216, 221, 225));
		plot.setDomainGridlinePaint(new Color(216-25, 221-25, 225-25));
		plot.setRangeGridlinePaint(new Color(216-25, 221-25, 225-25));
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
		
		plot.getDomainAxis().setUpperMargin(plot.getDomainAxis().getUpperMargin() + 0.20);

		XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) plot.getRenderer();
		for (int i = 0; i < table.getColumnCount() && i < colors.size(); i++) {
			r.setSeriesPaint(i, colors.get(i));
			r.setSeriesItemLabelPaint(i, colors.get(i));
			r.setSeriesStroke(i, new BasicStroke(1.5f));
		}
		DecimalFormat format = new DecimalFormat(numberFormat);
		NumberAxis na = (NumberAxis) plot.getRangeAxis();
		na.setNumberFormatOverride(format);

		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat(dateFormat.equals("") ? "yyyy-MM-dd" : dateFormat));

        XYItemLabelGenerator generator = new StandardXYItemLabelGenerator() {
			private static final long serialVersionUID = 1L;

			@Override
			public String generateLabel(XYDataset dataset, int series, int item) {
				return item != dataset.getItemCount(0) - 1 ? "" : data.headers.get(dataset.getSeriesCount() - series - 1) +
						" " + format.format(dataset.getYValue(series, item));
			}
        	
        };
        r.setDefaultItemLabelGenerator(generator);
        r.setDefaultItemLabelsVisible(true);
        r.setDefaultNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER_LEFT));
        r.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.CENTER_LEFT));
        r.setDefaultItemLabelFont(new Font("SansSerif", Font.PLAIN, 11));

        SVGGraphics2D g2 = new SVGGraphics2D(width, height);
        g2.setFontMapper(new FontMapper() {

			@Override
			public String mapFont(String family) {
				return "'Myriad Pro','Roboto', sans-serif";
			}});
        g2.setDefsKeyPrefix(id);
        chart.setElementHinting(true);
        chart.draw(g2, new Rectangle(width, height));
        return g2.getSVGElement(chart.getID());
	}

	public static String barChart(Table table, String id, String title, String dateFormat,
			String numberFormat, int width, int height, List<Color> colors) {

		JFreeChart chart = ChartFactory.createXYBarChart(
				title.equals("") ? null : "           " + title, // title
				null, // x-axis label
				true,
				null, // y-axis label
				new TableDataSet(table), // data
				PlotOrientation.VERTICAL,
				true,
				true, // generate tooltips?
				false // generate URLs?
		);
		if(!title.equals("")) {
			chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 12));
			chart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
		}
		//chart.setBackgroundPaint(new Color(216,221,225));
		XYPlot plot = (XYPlot) chart.getPlot();
		//plot.setBackgroundPaint(new Color(174, 180, 187));
		//plot.setDomainGridlinePaint(Color.white);
		//plot.setRangeGridlinePaint(Color.white);
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(new Color(216, 221, 225));
		plot.setRangeGridlinePaint(new Color(216, 221, 225));
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
		
		
		//chart.getLegend().setBackgroundPaint(new Color(216, 221, 225));
		//chart.getLegend().setBorder(new BlockBorder(new Color(174, 180, 187)));

		XYItemRenderer r = plot.getRenderer();
		for (int i = 0; i < table.getColumnCount() && i < colors.size(); i++)
			r.setSeriesPaint(i, colors.get(i));
		((XYBarRenderer) r).setBarPainter(new StandardXYBarPainter());
		((XYBarRenderer) r).setMargin(0.05);
		NumberAxis na = (NumberAxis) plot.getRangeAxis();
		na.setNumberFormatOverride(new DecimalFormat(numberFormat));
		//plot.setRangeAxis(0,na);
	

		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat(dateFormat.equals("") ? "yyyy-MM-dd" : dateFormat));

        SVGGraphics2D g2 = new SVGGraphics2D(width, height);
        g2.setFontMapper(new FontMapper() {

			@Override
			public String mapFont(String family) {
				return "'Myriad Pro', 'Roboto', sans-serif";
			}});
        g2.setDefsKeyPrefix(id);
        chart.setElementHinting(true);
        chart.draw(g2, new Rectangle(width, height));
        return g2.getSVGElement(chart.getID());
	}
	
	
	
	public static class TableDataSet implements XYDataset, IntervalXYDataset {
		
		private final List<LocalDate> dates;
		private final List<LocalDate> startDates;
		private final List<String> headers;
		private final double[][] columns;
		
		public TableDataSet(Table table) {
			this.dates = table.getRange().dates();
			this.startDates = table.getRange().expand(-1).dates();
			this.headers = table.getHeaders(false, false);
			this.columns = table.toColumnMajorArray();
		}

		@Override
		public DomainOrder getDomainOrder() {
			return DomainOrder.ASCENDING;
		}

		@Override
		public int getItemCount(int arg0) {
			return dates.size();
		}

		@Override
		public double getYValue(int arg0, int arg1) {
			double d = columns[columns.length - arg0 - 1][arg1];
			return Double.isInfinite(d) ? Double.NaN : d;
		}

		@Override
		public double getXValue(int arg0, int arg1) {
			return dates.get(arg1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
		}

		@Override
		public int getSeriesCount() {
			return columns.length;
		}

		@Override
		public Comparable<String> getSeriesKey(int arg0) {
			return headers.get(columns.length - arg0 - 1);
		}

		@Override
		public int indexOf(@SuppressWarnings("rawtypes") Comparable arg0) {
			return columns.length - headers.indexOf(arg0) - 1;
		}

		@Override
		public Number getX(int arg0, int arg1) {
			return getXValue(arg0,arg1);
		}

		@Override
		public Number getY(int arg0, int arg1) {
			return getYValue(arg0,arg1);
		}

		@Override public void addChangeListener(DatasetChangeListener arg0) { }
		@Override public DatasetGroup getGroup() { return null; }
		@Override public void removeChangeListener(DatasetChangeListener arg0) { } 
		@Override public void setGroup(DatasetGroup arg0) { }


		@Override
		public double getStartXValue(int series, int item) {
			return startDates.get(item).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 1;
		}


		@Override
		public double getEndXValue(int series, int item) {
			return getXValue(series, item);
		}

		@Override
		public double getStartYValue(int series, int item) {
			return getYValue(series, item);
		}


		@Override
		public double getEndYValue(int series, int item) {
			return getYValue(series, item);
		} 

		@Override public Number getStartX(int series, int item) { return getStartXValue(series,item); }
		@Override public Number getStartY(int series, int item) { return getStartYValue(series,item); }
		@Override public Number getEndX(int series, int item) { return getEndXValue(series,item); }
		@Override public Number getEndY(int series, int item) { return getEndYValue(series, item); }

		
	}

}
