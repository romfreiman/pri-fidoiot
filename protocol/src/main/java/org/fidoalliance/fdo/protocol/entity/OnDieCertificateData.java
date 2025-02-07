// Copyright 2022 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.fidoalliance.fdo.protocol.entity;

import java.sql.Blob;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;


@Entity
@Table(name = "ondie_certificate_data")
public class OnDieCertificateData {

  @Id
  @Column(name = "id", nullable = false)
  private String name;

  @Lob
  @Column(name = "data")
  private Blob data;

  public String getId() {
    return name;
  }

  public Blob getData() {
    return data;
  }

  public void setId(String name) {
    this.name = name;
  }

  public void setData(Blob blob) {
    this.data = blob;
  }
}
