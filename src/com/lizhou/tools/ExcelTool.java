package com.lizhou.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.model.HSSFRowRecord;
import org.apache.poi.hssf.model.WorkbookRecord;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BooleanPropertySetBlock;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.ColumnInfoRecord;
import org.apache.poi.hssf.record.CommonObjectDataSubRecord;
import org.apache.poi.hssf.record.CountryRecord;
import org.apache.poi.hssf.record.EndObjectLinkSubRecord;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.FeatHdrRecord;
import org.apache.poi.hssf.record.FilePassRecord;
import org.apache.poi.hssf.record.FontRecord;
import org.apache.poi.hssf.record.FormatRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.HyperlinkRecord;
import org.apache.poi.hssf.record.InterfaceEndRecord;
import org.apache.poi.hssf.record.InterfaceHdrRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.MMSRecord;
import org.apache.poi.hssf.record.MergedCellsRegion;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.record.PaletteRecord;
import org.apache.poi.hssf.record.PaneRecord;
import org.apache.poi.hssf.record.RKRecord;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.SelectionRecord;
import org.apache.poi.hssf.record.SeriesTextRecord;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.record.StyleRecord;
import org.apache.poi.hssf.record.SubRecord;
import org.apache.poi.hssf.record.TableRecord;
import org.apache.poi.hssf.record.UnicodeString;
import org.apache.poi.hssf.record.WindowOneRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

public class ExcelTool<T> {
	
	/**
	 * 导入Excel
	 * @param type
	 * @param filePath
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws InvocationTargetException 
	 * @throws Exception
	 */
	public List<T> importExcel(Class<T> type, InputStream is) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
		// 创建对Excel工作簿文件的引用
		HSSFWorkbook workbook = new HSSFWorkbook(is); 
		/*
		 * 在Excel文档中，第一张工作表的缺省索引是0 
		 * 其语句为：HSSFSheet sheet = workbook.getSheetAt(0);
		 * 根据表名获取: HSSFSheet sheet = wookbook.getSheet("Sheet1");
		 */
		HSSFSheet sheet = workbook.getSheetAt(0);
		//获取到Excel文件中的所有行数
        int rows = sheet.getPhysicalNumberOfRows();
        //标题行
        HSSFRow titleRow = null;
        int r = 0;
        for(; r < rows; r++){
        	// 读取一行
            HSSFRow row = sheet.getRow(r);
            // 行不为空
            if (row != null) {
            	titleRow = row; break;
            }
        }
        List<T> list = new LinkedList<T>();
        //用于格式化excel中的数值文本
        DecimalFormat df = new DecimalFormat("0");
        //遍历行，提取数据并封装
        r++;
        for (; r <= rows; r++) {
            HSSFRow row = sheet.getRow(r);
            if (row != null) {
            	T obj = type.newInstance();
            	//获取到该行中的所有的列
            	int cells = row.getPhysicalNumberOfCells();
                //遍历列
                for (int j = 0; j <= cells; j++) {
                	//获取到列的值
                    HSSFCell cell = row.getCell(j);
                    //获取列名
                    if (cell != null) {
                    	String name = titleRow.getCell(j).getStringCellValue();
                    	String value = "";
                    	switch (cell.getCellType()) {
                        	case HSSFCell.CELL_TYPE_FORMULA:
                        		break;
                            case HSSFCell.CELL_TYPE_NUMERIC:
                            	value = df.format(cell.getNumericCellValue());        
                                break;  
                        	case HSSFCell.CELL_TYPE_STRING:
                             	value = cell.getStringCellValue();
                                break;
                         	default:
                              	value = "";
                                break;
                    	}
                    	BeanUtils.setProperty(obj, name, value);
                    }      
                }
                list.add(obj);
            }
        }
		return list;
	}
	
	/**
	 * 导出
	 * @param headers
	 * @param list
	 * @param out
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public void exportExcel(String[] headers, List<T> list, OutputStream out) 
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet();
        // 生成一个样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        // 设置这些样式
        headerStyle.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
        headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        // 生成一个字体
        HSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
        // 把字体应用到当前的样式
        headerStyle.setFont(font);

        // 产生表格标题行
        HSSFRow row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellStyle(headerStyle);
            //HSSFRichTextString text = new HSSFRichTextString(headers[i]);
            cell.setCellValue(headers[i]);
        }
        // 遍历集合数据，产生数据行
        int index = 0;
        for(T t : list) {
            index++;
            row = sheet.createRow(index);
            for (int i = 0; i < headers.length; i++) {
                HSSFCell cell = row.createCell(i);
                String value = BeanUtils.getProperty(t, headers[i]);
                cell.setCellValue(value);
            }
        }
        //输出
        try {
        	workbook.write(out);
    		out.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }

    }
	
	public void exportMapExcel(String[] headers, List<Map<String, Object>> list, OutputStream out) 
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet();
        // 生成一个样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        // 设置这些样式
//        headerStyle.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
//        headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        // 生成一个字体
//        HSSFFont font = workbook.createFont();
//        font.setFontHeightInPoints((short) 12);
//        font.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
//        // 把字体应用到当前的样式
//        headerStyle.setFont(font);

        // 产生表格标题行
        HSSFRow row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellStyle(headerStyle);
            //HSSFRichTextString text = new HSSFRichTextString(headers[i]);
            cell.setCellValue(headers[i]);
        }
        // 遍历集合数据，产生数据行
        int index = 0;
        for(Map<String, Object> map : list) {
            index++;
            row = sheet.createRow(index);
            
            Set<Entry<String, Object>> set = map.entrySet();
            
            Iterator<Entry<String, Object>> it = set.iterator();
            int i = 0;
            while(it.hasNext()){
            	
            	Entry<String, Object> next = it.next();
            	Object value = next.getValue();
            	String key = next.getKey();
            	if(!key.contains("escoreid")){
            		HSSFCell cell = row.createCell(i++);
                	if(value instanceof String){
                		String str = (String) value;
                		cell.setCellValue(str);
                	} else{
                		Integer score = (Integer) value;
                		cell.setCellValue(score.intValue());
                	}
            	}
            }
        }
        //输出
        try {
        	workbook.write(out);
    		out.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }

    }

	public int importExcelStream(Class<T> type, InputStream is, Consumer<T> rowProcessor, int batchSize)
			throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
		if (batchSize <= 0) {
			batchSize = 1000;
		}
		DecimalFormat df = new DecimalFormat("0");

		HSSFEventFactory factory = new HSSFEventFactory();
		HSSFRequest request = new HSSFRequest();
		request.setIntercepting(false);

		StreamListener listener = new StreamListener(type, rowProcessor, df, batchSize);
		request.addListener(listener);

		factory.processWorkbookEvents(request, is);

		listener.flushRemaining();

		return listener.getProcessedRows();
	}

	public static class AbortableStreamListener extends StreamListener {
		public AbortableStreamListener(Class<?> type, Consumer<T> rowProcessor, DecimalFormat df, int batchSize) {
			super(type, rowProcessor, df, batchSize);
		}
	}

	public static class StreamListener implements HSSFListener {
		private final Class<?> type;
		private final Consumer<T> rowProcessor;
		private final DecimalFormat df;
		private final int batchSize;
		private SSTRecord[] sharedStrings;
		private int currentRow = -1;
		private Object[] currentRowData;
		private int maxCells = 0;
		private int processedRows = 0;
		private boolean processingTitle = true;
		private String[] titleNames;
		private int batchCount = 0;
		private volatile boolean aborted = false;

		public StreamListener(Class<?> type, Consumer<T> rowProcessor, DecimalFormat df, int batchSize) {
			this.type = type;
			this.rowProcessor = rowProcessor;
			this.df = df;
			this.batchSize = batchSize;
		}

		public void abort() {
			this.aborted = true;
		}

		public boolean isAborted() {
			return aborted;
		}

		@Override
		public void processRecord(org.apache.poi.hssf.record.Record record) {
			if (aborted) {
				return;
			}

			short sid = record.getSid();

			if (sid == SSTRecord.sid) {
				sharedStrings = ((SSTRecord) record).getRecords();
				return;
			}

			if (sid == RowRecord.sid) {
				RowRecord rowRec = (RowRecord) record;
				int rowNum = rowRec.getRowNumber();

				if (processingTitle && rowNum == 0) {
					currentRow = 0;
					maxCells = rowRec.getLastCellNum();
					if (maxCells > 0) {
						titleNames = new String[maxCells];
						currentRowData = new Object[maxCells];
					}
					return;
				}

				if (currentRowData != null && hasData(currentRowData)) {
					deliverRow();
				}

				currentRow = rowNum;
				maxCells = rowRec.getLastCellNum();
				currentRowData = new Object[maxCells];
				return;
			}

			if (processingTitle) {
				if (sid == LabelSSTRecord.sid && currentRow == 0) {
					LabelSSTRecord labelRec = (LabelSSTRecord) record;
					int cellIdx = labelRec.getColumn();
					if (cellIdx < maxCells && sharedStrings != null) {
						titleNames[cellIdx] = sharedStrings[labelRec.getSSTIndex()].getString();
					}
				}
				return;
			}

			if (currentRowData == null) {
				return;
			}

			if (sid == LabelSSTRecord.sid) {
				LabelSSTRecord labelRec = (LabelSSTRecord) record;
				int cellIdx = labelRec.getColumn();
				if (cellIdx < maxCells && sharedStrings != null) {
					currentRowData[cellIdx] = sharedStrings[labelRec.getSSTIndex()].getString();
				}
				return;
			}

			if (sid == NumberRecord.sid) {
				NumberRecord numRec = (NumberRecord) record;
				int cellIdx = numRec.getColumn();
				if (cellIdx < maxCells) {
					currentRowData[cellIdx] = df.format(numRec.getValue());
				}
				return;
			}

			if (sid == LabelRecord.sid) {
				LabelRecord labelRec = (LabelRecord) record;
				int cellIdx = labelRec.getColumn();
				if (cellIdx < maxCells) {
					currentRowData[cellIdx] = labelRec.getText();
				}
				return;
			}

			if (sid == RKRecord.sid) {
				RKRecord rkRec = (RKRecord) record;
				int cellIdx = rkRec.getColumn();
				if (cellIdx < maxCells) {
					currentRowData[cellIdx] = df.format(rkRec.getRKNumber());
				}
				return;
			}

			if (sid == BOFRecord.sid) {
				BOFRecord bofRec = (BOFRecord) record;
				if (bofRec.getType() == BOFRecord.CALCSET) {
					processingTitle = false;
				}
				return;
			}
		}

		private boolean hasData(Object[] row) {
			if (row == null) return false;
			for (Object cell : row) {
				if (cell != null && !cell.toString().isEmpty()) {
					return true;
				}
			}
			return false;
		}

		@SuppressWarnings("unchecked")
		private void deliverRow() {
			try {
				Object obj = type.newInstance();
				for (int i = 0; i < titleNames.length && i < currentRowData.length; i++) {
					if (titleNames[i] != null && currentRowData[i] != null) {
						BeanUtils.setProperty(obj, titleNames[i], currentRowData[i]);
					}
				}
				rowProcessor.accept((T) obj);
				processedRows++;
				batchCount++;

				if (batchCount >= batchSize) {
					batchCount = 0;
				}
			} catch (Exception e) {
				throw new RuntimeException("Error processing row " + currentRow, e);
			}
		}

		public void flushRemaining() {
			if (currentRowData != null && hasData(currentRowData) && !aborted) {
				deliverRow();
			}
		}

		public int getProcessedRows() {
			return processedRows;
		}
	}

	public static class ExcelRowIterator implements Iterator<Object[]>, AutoCloseable {
		private final InputStream is;
		private final HSSFEventFactory factory = new HSSFEventFactory();
		private SSTRecord[] sharedStrings;
		private Object[] currentRowData;
		private int currentRow = -1;
		private int maxCells = 0;
		private boolean hasNext = false;
		private boolean closed = false;
		private boolean titleExtracted = false;
		private boolean processingTitle = true;
		private DecimalFormat df = new DecimalFormat("0");
		private boolean endOfData = false;

		public ExcelRowIterator(InputStream is) {
			this.is = is;
		}

		private void ensureInitialized() {
			if (currentRowData == null && !endOfData) {
				processNextBatch();
			}
		}

		private void processNextBatch() {
			IteratorListener listener = new IteratorListener(this);
			HSSFRequest request = new HSSFRequest();
			request.setIntercepting(false);
			try {
				factory.processWorkbookEvents(request, is);
				endOfData = true;
			} catch (IOException e) {
				throw new RuntimeException("Error reading Excel stream", e);
			}
		}

		@Override
		public boolean hasNext() {
			ensureInitialized();
			return hasNext && !endOfData;
		}

		@Override
		public Object[] next() {
			if (!hasNext()) {
				throw new java.util.NoSuchElementException();
			}
			Object[] row = currentRowData;
			currentRowData = null;
			hasNext = false;
			processNextBatch();
			return row;
		}

		@Override
		public void close() throws IOException {
			if (!closed) {
				is.close();
				closed = true;
			}
		}

		private static class IteratorListener implements HSSFListener {
			private final ExcelRowIterator iterator;
			private SSTRecord[] sharedStrings;

			public IteratorListener(ExcelRowIterator iterator) {
				this.iterator = iterator;
			}

			@Override
			public void processRecord(org.apache.poi.hssf.record.Record record) {
				short sid = record.getSid();

				if (sid == SSTRecord.sid) {
					sharedStrings = ((SSTRecord) record).getRecords();
					return;
				}

				if (sid == RowRecord.sid) {
					RowRecord rowRec = (RowRecord) record;
					if (iterator.processingTitle && rowRec.getRowNumber() == 0) {
						iterator.maxCells = rowRec.getLastCellNum();
					} else {
						iterator.currentRow = rowRec.getRowNumber();
						iterator.maxCells = rowRec.getLastCellNum();
						iterator.currentRowData = new Object[iterator.maxCells];
						iterator.hasNext = true;
						iterator.processingTitle = false;
					}
					return;
				}

				if (iterator.processingTitle || iterator.currentRowData == null) {
					return;
				}

				if (sid == LabelSSTRecord.sid) {
					LabelSSTRecord labelRec = (LabelSSTRecord) record;
					int cellIdx = labelRec.getColumn();
					if (cellIdx < iterator.maxCells && sharedStrings != null) {
						iterator.currentRowData[cellIdx] = sharedStrings[labelRec.getSSTIndex()].getString();
					}
					return;
				}

				if (sid == NumberRecord.sid) {
					NumberRecord numRec = (NumberRecord) record;
					int cellIdx = numRec.getColumn();
					if (cellIdx < iterator.maxCells) {
						iterator.currentRowData[cellIdx] = iterator.df.format(numRec.getValue());
					}
					return;
				}

				if (sid == BOFRecord.sid) {
					BOFRecord bofRec = (BOFRecord) record;
					if (bofRec.getType() == BOFRecord.CALCSET && !iterator.titleExtracted) {
						iterator.processingTitle = false;
						iterator.titleExtracted = true;
					}
					return;
				}
			}
		}
	}

}
