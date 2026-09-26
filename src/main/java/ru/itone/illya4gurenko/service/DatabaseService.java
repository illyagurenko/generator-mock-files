package ru.itone.illya4gurenko.service;


import ru.itone.illya4gurenko.config.Base;
import ru.itone.illya4gurenko.struct_file.GruVistaTab;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class DatabaseService extends Base {

    private static final DatabaseService INSTANCE = new DatabaseService();
    private static final int BATCH_SIZE = 1000;

    private DatabaseService() {}

    public static DatabaseService getInstance() {
        return INSTANCE;
    }

    public void insertGruVistaBatch(List<GruVistaTab> records) throws SQLException {
        String sql = """
            INSERT INTO GRU.GRU_VISTA_TAB (
                ID, SYSTEMACCOUNT, CURRENCY, XALFA, OPERATION, TIME_STAMP, 
                POM_ID, UTERRARIO, OLDTBAL, NEWTBAL, ADD_INFO, FILE_ID, 
                FOC_STATUS, FOC_TS, FOC_TYPE
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        info("Starting batch insert of {} records into GRU.GRU_VISTA_TAB", records.size());

        try (Connection conn = DriverManager.getConnection(
                config.getDbUrl(), config.getDbUser(), config.getDbPassword());
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            int count = 0;

            for (GruVistaTab record : records) {
                ps.setObject(1, record.getId());
                ps.setString(2, record.getSystemAccount());
                ps.setString(3, record.getCurrency());
                ps.setBigDecimal(4, record.getXalfa());
                ps.setString(5, record.getOperation() != null ? record.getOperation().name() : null);
                ps.setTimestamp(6, record.getTimeStamp() != null ? Timestamp.valueOf(record.getTimeStamp()) : null);
                ps.setObject(7, record.getPomId());
                ps.setObject(8, record.getUterrario());
                ps.setBigDecimal(9, record.getOldTBal());
                ps.setBigDecimal(10, record.getNewTBal());
                ps.setString(11, record.getAddInfo());
                ps.setObject(12, record.getFileId());
                ps.setString(13, record.getFocStatus() != null ? record.getFocStatus().name() : null);
                ps.setTimestamp(14, record.getFocTS() != null ? Timestamp.valueOf(record.getFocTS()) : null);
                ps.setString(15, record.getFocType() != null ? record.getFocType().name() : null);

                ps.addBatch();
                count++;

                if (count % BATCH_SIZE == 0) {
                    ps.executeBatch();
                }
            }

            ps.executeBatch();
            conn.commit();
            info("Batch insert completed successfully");

        } catch (SQLException e) {
            error("Database insertion failed", e);
            throw e;
        }
    }
}