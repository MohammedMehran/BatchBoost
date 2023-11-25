package com.temenos.hackathon.bulkprocess.model;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 *
 * @author leena.ganta
 *
 */

@Component
public class Hacker {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String employeeId;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String employeeName;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String department;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String mobileNumber;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String email;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String accountNo;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String couponTransfered;

    /**
     * @return the couponTransfered
     */
    public String getCouponTransfered() {
        return couponTransfered;
    }

    /**
     * @param couponTransfered
     *            the couponTransfered to set
     */
    public void setCouponTransfered(String couponTransfered) {
        this.couponTransfered = couponTransfered;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public Hacker() {
    }

    /**
     * @param employeeId
     * @param employeeName
     * @param department
     * @param mobileNumber
     * @param email
     * @param accountNo
     * @param couponTransfered
     */
    public Hacker(String employeeId, String employeeName, String department, String mobileNumber, String email,
            String accountNo, String couponTransfered) {

        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.accountNo = accountNo;
        this.couponTransfered = couponTransfered;
    }

    @Override
    public String toString() {
        return "Hacker [employeeId=" + employeeId + ", employeeName=" + employeeName + ", department=" + department
                + ", mobileNumber=" + mobileNumber + ", email=" + email + ", accountNo=" + accountNo
                + ", couponTransfered=" + couponTransfered + "]";
    }

}
