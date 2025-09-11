package com.empresa.proyecto.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "deceased")
public class Deceased {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "national_id", length = 15)
    private String nationalId;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "paternal_last_name", length = 50)
    private String paternalLastName;

    @Column(name = "maternal_last_name", length = 50)
    private String maternalLastName;

    @Column(name = "death_date")
    private LocalDate deathDate;

    private Integer age;

    private String genre;

    @Column(name = "death_place", length = 100)
    private String deathPlace;

    @Column(length = 100)
    private String source;

    @Column(length = 150)
    private String cause;

    @Column(name = "geo_code", length = 6)
    private String geoCode;

    @Column(length = 20)
    private String marital;

    public Deceased() {
    }

    public Deceased(Long id, String nationalId, String firstName, String paternalLastName, String maternalLastName, LocalDate deathDate, Integer age, String genre, String deathPlace, String source, String cause, String geoCode, String marital) {
        this.id = id;
        this.nationalId = nationalId;
        this.firstName = firstName;
        this.paternalLastName = paternalLastName;
        this.maternalLastName = maternalLastName;
        this.deathDate = deathDate;
        this.age = age;
        this.genre = genre;
        this.deathPlace = deathPlace;
        this.source = source;
        this.cause = cause;
        this.geoCode = geoCode;
        this.marital = marital;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPaternalLastName() {
        return paternalLastName;
    }

    public void setPaternalLastName(String paternalLastName) {
        this.paternalLastName = paternalLastName;
    }

    public String getMaternalLastName() {
        return maternalLastName;
    }

    public void setMaternalLastName(String maternalLastName) {
        this.maternalLastName = maternalLastName;
    }

    public LocalDate getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(LocalDate deathDate) {
        this.deathDate = deathDate;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDeathPlace() {
        return deathPlace;
    }

    public void setDeathPlace(String deathPlace) {
        this.deathPlace = deathPlace;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getGeoCode() {
        return geoCode;
    }

    public void setGeoCode(String geoCode) {
        this.geoCode = geoCode;
    }

    public String getMarital() {
        return marital;
    }

    public void setMarital(String marital) {
        this.marital = marital;
    }
}