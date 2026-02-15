package com.example.backend.services.analytics.excel;

import com.example.backend.models.UserModel;
import com.example.backend.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelService implements IExcelService{

    private final UserService userService;

    public byte[] exportUsersToExcel(String email) throws Exception {
        List<UserModel> users = userService.getAllUsers(email);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        CreationHelper createHelper = workbook.getCreationHelper();

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        CellStyle textStyle = workbook.createCellStyle();
        textStyle.setAlignment(HorizontalAlignment.LEFT);

        CellStyle centerStyle = workbook.createCellStyle();
        centerStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));
        dateStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Nickname", "Role", "Email", "IsEmailVerified", "CreatedAt", "UpdatedAt"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIndex = 1;
        for (UserModel user : users) {
            Row row = sheet.createRow(rowIndex++);

            Cell idCell = row.createCell(0);
            idCell.setCellValue(String.valueOf(user.getId()));
            idCell.setCellStyle(centerStyle);

            Cell nickCell = row.createCell(1);
            nickCell.setCellValue(user.getNickname());
            nickCell.setCellStyle(textStyle);

            Cell roleCell = row.createCell(2);
            roleCell.setCellValue(user.getRole().toString());
            roleCell.setCellStyle(centerStyle);

            Cell emailCell = row.createCell(3);
            emailCell.setCellValue(user.getEmail());
            emailCell.setCellStyle(textStyle);

            Cell verifiedCell = row.createCell(4);
            verifiedCell.setCellValue(user.isEmailVerified() ? "Yes" : "No");
            verifiedCell.setCellStyle(centerStyle);

            Cell createdAtCell = row.createCell(5);
            createdAtCell.setCellValue(java.sql.Date.valueOf(user.getCreatedAt()));
            createdAtCell.setCellStyle(dateStyle);

            Cell updatedAtCell = row.createCell(6);
            updatedAtCell.setCellValue(java.sql.Date.valueOf(user.getUpdatedAt()));
            updatedAtCell.setCellStyle(dateStyle);
        }

        for (int i = 0; i <= 6; i++) sheet.autoSizeColumn(i);
        sheet.createFreezePane(0, 1);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }
}
