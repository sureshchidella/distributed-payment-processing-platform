package com.suresh.paymentsimulator.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a currency with its metadata.
 * Loaded from currencies.json at application startup.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Currency {

    @JsonProperty("name")
    private String name;

    @JsonProperty("demonym")
    private String demonym;

    @JsonProperty("majorSingle")
    private String majorSingle;

    @JsonProperty("majorPlural")
    private String majorPlural;

    @JsonProperty("ISOnum")
    private Integer isoNum;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("symbolNative")
    private String symbolNative;

    @JsonProperty("minorSingle")
    private String minorSingle;

    @JsonProperty("minorPlural")
    private String minorPlural;

    @JsonProperty("ISOdigits")
    private Integer isoDigits;

    @JsonProperty("decimals")
    private Integer decimals;

    @JsonProperty("numToBasic")
    private Integer numToBasic;
}