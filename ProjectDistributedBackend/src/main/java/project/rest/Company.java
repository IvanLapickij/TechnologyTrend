package project.rest;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "company")
public class Company {

 private int companyID;
 private String companyName;
 private int years;

 public Company() {
     // JAXB requires a no-arg constructor
 }

 public int getCompanyID() {
     return companyID;
 }

 public void setCompanyID(int companyID) {
     this.companyID = companyID;
 }

 public String getCompanyName() {
     return companyName;
 }

 public void setCompanyName(String companyName) {
     this.companyName = companyName;
 }

 public int getYears() {
     return years;
 }

 public void setYears(int years) {
     this.years = years;
 }
}

