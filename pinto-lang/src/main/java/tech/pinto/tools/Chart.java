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
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.graphics2d.svg.FontMapper;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import tech.pinto.Table;
import tech.pinto.time.Period;

public class Chart {
	
//	private static final Color[] SERIES_COLORS = new Color[] {
//			new Color(251, 201, 19),
//			new Color(214, 236, 0),
//			new Color(118, 124, 187),
//			new Color(183, 239, 204),
//			new Color(241, 144, 101),
//			new Color(205, 205, 207),
//			new Color(81, 81, 81),
//			new Color(222, 190, 160) };

	private static final Color[] SERIES_COLORS = new Color[] {
			new Color(18, 66, 109),
			new Color(35, 131, 219),
			new Color(133, 186, 235),
			new Color(195, 221, 245),
			new Color(170, 218, 172),
			new Color(72, 148, 82),
			new Color(48, 98, 54),
			new Color(4, 64, 11),
			new Color(40, 40, 40),
			new Color(81, 81, 81),
			new Color(128, 128, 128),
			new Color(205, 205, 205),
			new Color(173, 176, 214),
			new Color(118, 124, 187),
			new Color(85, 86, 170),
			new Color(59, 60, 141)
	};

	public static String lineChart(Table table, String id, String title, String dateFormat,
			String numberFormat, int width, int height ) {

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				title.equals("") ? null : "           " + title, // title
				null, // x-axis label
				null, // y-axis label
				new TableDataSet(table), // data
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
		for (int i = 0; i < table.getColumnCount() && i < SERIES_COLORS.length; i++)
			r.setSeriesPaint(i, SERIES_COLORS[i]);
		if (r instanceof XYLineAndShapeRenderer) {
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
			renderer.setDefaultStroke(new BasicStroke(3));
			//renderer.setDefaultShapesVisible(true);
			//renderer.setDefaultShapesFilled(true);
		}
		NumberAxis na = (NumberAxis) plot.getRangeAxis();
		na.setNumberFormatOverride(new DecimalFormat(numberFormat));
		//plot.setRangeAxis(0,na);
	

		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat(dateFormat.equals("") ? "yyyy-MM-dd" : dateFormat));

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
			String numberFormat, int width, int height ) {

		JFreeChart chart = ChartFactory.createXYBarChart(
				title.equals("") ? null : "           " + title, // title
				null, // x-axis label
				true,
				null, // y-axis label
				new TableDataSet(table), // data
//				new XYBarDataset(new TableDataSet(table), 900000000), // data
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
		for (int i = 0; i < table.getColumnCount() && i < SERIES_COLORS.length; i++)
			r.setSeriesPaint(i, SERIES_COLORS[i]);
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
			return columns[arg0][arg1];
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
			return headers.get(arg0);
		}

		@Override
		public int indexOf(@SuppressWarnings("rawtypes") Comparable arg0) {
			return headers.indexOf(arg0);
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
