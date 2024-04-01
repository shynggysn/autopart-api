package kz.csi.test_task.service;

import jakarta.transaction.Transactional;
import kz.csi.test_task.dto.AutopartDto;
import kz.csi.test_task.entity.Autopart;
import kz.csi.test_task.mapper.AutopartMapper;
import kz.csi.test_task.payload.request.AddPartRequest;
import kz.csi.test_task.repository.AutopartRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AutopartService {
    private final AutopartRepository repository;
    private final AutopartMapper autopartMapper;
    private Integer index;

    @Transactional
    public AutopartDto addAutopart(AddPartRequest req) {
        if (Objects.isNull(req.getParentId()))
            return autopartMapper.map(repository.save(new Autopart(req.getDetailName(), req.getPrice(), req.getQuantity())));

        var parentOptional = repository.findById(req.getParentId());
        if (parentOptional.isEmpty())
            return null;

        var autopart = new Autopart(req.getDetailName(), req.getPrice(), req.getQuantity(), parentOptional.get());

        var temp= autopart;
        int tempPrice = 0;
        while (Objects.nonNull(temp.getParent())) {
            var parent = temp.getParent();
            if (repository.findByParent(parent).isEmpty()) {
                tempPrice = parent.getPrice();
                parent.setPrice(req.getPrice());
                parent.setTotal(parent.getPrice() * parent.getQuantity());
            } else {
                if (tempPrice != 0)
                    parent.setPrice(parent.getPrice() - tempPrice);
                parent.setPrice(parent.getPrice() + req.getPrice());
                parent.setTotal(parent.getPrice() * parent.getQuantity());
            }
            parent = repository.save(parent);
            temp = parent;
        }

        return autopartMapper.map(repository.save(autopart));
    }

    @Transactional
    public boolean deleteAutopart(Long id) {
        var autopartOptional = repository.findById(id);
        if (autopartOptional.isEmpty())
            return false;

        var autopart = autopartOptional.get();
        var temp = autopart;
        while (Objects.nonNull(temp.getParent())) {
            var parent = temp.getParent();
            parent.setPrice(parent.getPrice() - autopart.getPrice());
            parent.setTotal(parent.getPrice() * parent.getQuantity());
            repository.save(parent);
            temp = parent;
        }
        repository.deleteById(id);
        return true;
    }

    public List<AutopartDto> getAutopartList() {
        return repository.findByParent(null).stream()
                .sorted(Comparator.comparing(Autopart::getId))
                .map(autopart -> sortChildrenRecursively(autopartMapper.map(autopart)))
                .collect(Collectors.toList());
    }

    private AutopartDto sortChildrenRecursively(AutopartDto autopartDto) {
        List<AutopartDto> sortedChildren = autopartDto.children().stream()
                .sorted(Comparator.comparing(AutopartDto::id))
                .map(this::sortChildrenRecursively)
                .collect(Collectors.toList());

        return new AutopartDto(
                autopartDto.id(),
                autopartDto.detailName(),
                autopartDto.price(),
                autopartDto.quantity(),
                autopartDto.total(),
                sortedChildren
        );
    }

    public Workbook generateExcel() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Cписок деталей");

        String[] columns = {"№", "Деталь", "Цена", "Количество", "Стоимость"};

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontName("SansSerif");
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        Font dataFont = workbook.createFont();
        dataFont.setFontName("SansSerif");
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setWrapText(true);
        dataCellStyle.setFont(dataFont);
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        List<Autopart> autoparts = repository.findByParent(null);

        index = 1;
        Integer visualIndex = 0;
        for (Autopart record : autoparts) {
            visualIndex++;
            createRowForAutopartAndChildren(record, String.valueOf(visualIndex), sheet, dataCellStyle);
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
        return workbook;
    }

    public void createRowForAutopartAndChildren(Autopart autopart, String indexValue, Sheet sheet, CellStyle dataCellStyle) {
        Row row = sheet.createRow(index);
        for (int i = 0; i < 5; i++)
            row.createCell(i);

        row.getCell(0).setCellValue(indexValue);
        row.getCell(1).setCellValue(autopart.getDetailName());
        row.getCell(2).setCellValue(autopart.getPrice());
        row.getCell(3).setCellValue(autopart.getQuantity());
        row.getCell(4).setCellValue(autopart.getTotal());
        row.setRowStyle(dataCellStyle);

        List<Autopart> children = autopart.getChildren();
        if (children.isEmpty()) {
            index++;
        }
        else {
            for (int i = 0; i < children.size(); i++) {
                index++;
                createRowForAutopartAndChildren(children.get(i), indexValue + "." + (i + 1), sheet, dataCellStyle);
            }
        }
    }
}
