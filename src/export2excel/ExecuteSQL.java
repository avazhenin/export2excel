/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package export2excel;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 *
 * @author vazhenin
 */
public class ExecuteSQL implements Runnable {

    MainFrame frame;
    ArrayList<String> cols = new ArrayList<String>();
    int row = 0;

    public ExecuteSQL(MainFrame mainWindow) {
        frame = mainWindow;
    }

    @Override
    public void run() {
        DButilities db = new DButilities();
        Logging.setLogFileName("DebugFile.txt");
        int dataRow = 1;
        int maxrowsperpage = Integer.parseInt(frame.getjTextField_maxrowsperpage());
        try {
            /* set database parameters */
            db.setUser(frame.getJtext_username());
            db.setPassword(frame.getjText_Password());
            db.setDatabase_url("jdbc:oracle:thin:@" + frame.getjComboBoxHost() + ":1521:" + frame.getjText_SID());
            db.open_conn();

            System.out.println("Connected");

        } catch (Exception e) {
            Logging.put_log(e);
        }
        try {

            WorkbookSettings settings = new WorkbookSettings();
            settings.setGCDisabled(true);
            settings.setUseTemporaryFileDuringWrite(true);
            settings.setTemporaryFileDuringWriteDirectory(new File("."));

            WritableWorkbook workbook = Workbook.createWorkbook(new File("myfile" + System.currentTimeMillis() + ".xls"), settings);
            int sheetnum = 0;

            WritableSheet sheet = workbook.createSheet("Sheet " + sheetnum, sheetnum);

            frame.setjLabelResult("Выполнение SQL");
            db.executeQuery(frame.getjTextArea_SQL());
            ResultSet rs = db.getRs();

            cols.add("row number");
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                cols.add(rs.getMetaData().getColumnName(i + 1));
            }

            insertHeader(sheet);

            while (rs.next()) {
                for (int i = 0; i < cols.size(); i++) {
                    Label label = new Label(i, row, (i == 0) ? (String.valueOf(dataRow)) : (rs.getString(i)));
                    sheet.addCell(label);
                }
                row++;
                dataRow++;

                if (row >= maxrowsperpage + 1/* +1 means we don't count header as a row */) {
                    row = 0;
//                    workbook.write();
                    sheetnum++;
                    sheet = workbook.createSheet("Sheet " + sheetnum, sheetnum);
                    insertHeader(sheet);
                }
            }

            row = 0;
            cols = new ArrayList<String>();
            dataRow = 0;
            workbook.write();
            workbook.close();
            frame.setjLabelResult("Выполнение SQL завершено");
        } catch (Exception e) {
            e.printStackTrace();
            frame.setjLabelResult("Возникла ошибка при выполнении SQL, см лог файл");
            Logging.put_log(e);
        }
    }

    void insertHeader(WritableSheet currentSheet) {
        try {
            for (int i = 0; i < cols.size(); i++) {
                currentSheet.addCell(new Label(i, row, cols.get(i)));
            }
        } catch (Exception e) {
            Logging.put_log(e);
        }

        row++; /* increment row number after we've inserted header */

    }

}
