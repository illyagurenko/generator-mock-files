package ru.itone.illya4gurenko.struct_file;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GruVistaTab {

    private Long id;

    private String systemAccount;

    private String currency;

    private BigDecimal xalfa;

    private Type operation;

    private LocalDateTime timeStamp;

    private Long pomId;

    private Long uterrario;

    private BigDecimal oldTBal;

    private BigDecimal newTBal;

    private String addInfo;

    private Long fileId;

    private FocStatus focStatus;

    private LocalDateTime focTS;

    private ProcType focType;

    public GruVistaTab(Long id,
                       String systemAccount,
                       String currency,
                       BigDecimal xalfa,
                       Type operation,
                       LocalDateTime timeStamp,
                       Long pomId,
                       Long uterrario,
                       BigDecimal oldTBal,
                       BigDecimal newTBal,
                       String addInfo,
                       Long fileId,
                       FocStatus focStatus,
                       LocalDateTime focTS,
                       ProcType focType,
                       String svfeLoadId) {
        this.id = id;
        this.systemAccount = systemAccount;
        this.currency = currency;
        this.xalfa = xalfa;
        this.operation = operation;
        this.timeStamp = timeStamp;
        this.pomId = pomId;
        this.uterrario = uterrario;
        this.oldTBal = oldTBal;
        this.newTBal = newTBal;
        this.addInfo = addInfo;
        this.fileId = fileId;
        this.focStatus = focStatus;
        this.focTS = focTS;
        this.focType = focType;
    }

    public Long getId() { return id; }
    public String getSystemAccount() { return systemAccount; }
    public String getCurrency() { return currency; }
    public BigDecimal getXalfa() { return xalfa; }
    public Type getOperation() { return operation; }
    public LocalDateTime getTimeStamp() { return timeStamp; }
    public Long getPomId() { return pomId; }
    public Long getUterrario() { return uterrario; }
    public BigDecimal getOldTBal() { return oldTBal; }
    public BigDecimal getNewTBal() { return newTBal; }
    public String getAddInfo() { return addInfo; }
    public Long getFileId() { return fileId; }
    public FocStatus getFocStatus() { return focStatus; }
    public LocalDateTime getFocTS() { return focTS; }
    public ProcType getFocType() { return focType; }

}
