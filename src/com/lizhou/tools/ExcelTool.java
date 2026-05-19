package com.lizhou.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

public class ExcelTool<T> {
	
	/**
	 * 单行数据处理器回调接口
	 */
	public interface RowProcessor<T> {
		void process(T row);
	}
	
	/**
	 * 批量数据处理器回调接口
	 */
	public interface BatchProcessor<T> {
		void processBatch(List<T> batch);
	}
	
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
	 * 流式导入Excel，支持批量处理和中断
	 * @param type 目标对象类型
	 * @param is 输入流
	 * @param rowProcessor 单行数据处理器
	 * @param batchSize 批量大小（0表示不批量）
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void importExcelStream(Class<T> type, InputStream is, RowProcessor<T> rowProcessor, int batchSize) throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
		importExcelStream(type, is, rowProcessor, null, batchSize);
	}
	
	/**
	 * 流式导入Excel，支持批量处理、批量回调和中断
	 * @param type 目标对象类型
	 * @param is 输入流
	 * @param rowProcessor 单行数据处理器
	 * @param batchProcessor 批量数据处理器（可选）
	 * @param batchSize 批量大小（0表示不批量）
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void importExcelStream(Class<T> type, InputStream is, RowProcessor<T> rowProcessor, BatchProcessor<T> batchProcessor, int batchSize) throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
		HSSFWorkbook workbook = new HSSFWorkbook(is);
		HSSFSheet sheet = workbook.getSheetAt(0);
		int rows = sheet.getPhysicalNumberOfRows();
		HSSFRow titleRow = null;
		int r = 0;
		
		for (; r < rows; r++) {
			HSSFRow row = sheet.getRow(r);
			if (row != null) {
				titleRow = row;
				break;
			}
		}
		
		DecimalFormat df = new DecimalFormat("0");
		List<T> batchList = new ArrayList<T>(batchSize > 0 ? batchSize : 100);
		r++;
		
		for (; r <= rows; r++) {
			HSSFRow row = sheet.getRow(r);
			if (row != null) {
				T obj = type.newInstance();
				int cells = row.getPhysicalNumberOfCells();
				
				for (int j = 0; j <= cells; j++) {
					HSSFCell cell = row.getCell(j);
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
				
				if (rowProcessor != null) {
					rowProcessor.process(obj);
				}
				
				if (batchSize > 0) {
					batchList.add(obj);
					if (batchList.size() >= batchSize) {
						if (batchProcessor != null) {
							batchProcessor.processBatch(new ArrayList<T>(batchList));
						}
						batchList.clear();
					}
				}
			}
		}
		
		if (batchSize > 0 && !batchList.isEmpty() && batchProcessor != null) {
			batchProcessor.processBatch(batchList);
		}
	}
	
	/**
	 * 流式导入Excel，返回行迭代器（可用于手动控制迭代过程和中断）
	 * @param type 目标对象类型
	 * @param is 输入流
	 * @return 行迭代器
	 * @throws IOException
	 */
	public ExcelRowIterator<T> importExcelIterator(Class<T> type, InputStream is) throws IOException {
		return new ExcelRowIterator<T>(type, is);
	}
	
	/**
	 * Excel行迭代器，用于手动控制迭代过程
	 */
	public static class ExcelRowIterator<T> implements Iterator<T> {
		private Class<T> type;
		private HSSFWorkbook workbook;
		private HSSFSheet sheet;
		private HSSFRow titleRow;
		private int currentRow;
		private int totalRows;
		private DecimalFormat df;
		
		public ExcelRowIterator(Class<T> type, InputStream is) throws IOException {
			this.type = type;
			this.workbook = new HSSFWorkbook(is);
			this.sheet = workbook.getSheetAt(0);
			this.totalRows = sheet.getPhysicalNumberOfRows();
			this.df = new DecimalFormat("0");
			
			for (currentRow = 0; currentRow < totalRows; currentRow++) {
				HSSFRow row = sheet.getRow(currentRow);
				if (row != null) {
					titleRow = row;
					break;
				}
			}
			currentRow++;
		}
		
		@Override
		public boolean hasNext() {
			return currentRow <= totalRows;
		}
		
		@Override
		public T next() {
			try {
				HSSFRow row = sheet.getRow(currentRow);
				currentRow++;
				
				if (row == null) {
					return null;
				}
				
				T obj = type.newInstance();
				int cells = row.getPhysicalNumberOfCells();
				
				for (int j = 0; j <= cells; j++) {
					HSSFCell cell = row.getCell(j);
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
				
				return obj;
			} catch (Exception e) {
				throw new RuntimeException("Error reading Excel row", e);
			}
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove operation is not supported");
		}
		
		public void close() throws IOException {
			if (workbook != null) {
				workbook.close();
			}
		}
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
	
	
}
