-- This is the initial script for setting up the data base for this plugin.
-- For specific data base dialects, place sql scripts in the sub directory init/{vendor}

CREATE TABLE t_plugin_travel (
  pk                           INTEGER NOT NULL,
  created                      TIMESTAMP WITHOUT TIME ZONE,
  deleted                      BOOLEAN NOT NULL,
  last_update                  TIMESTAMP WITHOUT TIME ZONE,
  employee_id                  INTEGER NOT NULL,
  reason_of_travel             CHARACTER VARYING(4000),
  start_location               CHARACTER VARYING(30),
  return_location              CHARACTER VARYING(30),
  destination                  CHARACTER VARYING(255),
  kost2_id                     INTEGER,
  begin_of_travel              TIMESTAMP,
  end_of_travel                TIMESTAMP,
  hotel                        BOOLEAN NOT NULL,
  rental_car                   BOOLEAN NOT NULL,
  train                        BOOLEAN NOT NULL,
  flight                       BOOLEAN NOT NULL,
  kilometers                   INTEGER,
  tenant_id                    INTEGER,
  attachments_names            CHARACTER VARYING(10000),
  attachments_ids              CHARACTER VARYING(10000),
  attachments_size             SMALLINT,
  attachments_last_user_action CHARACTER VARYING(10000),
  assumption_of_costs          CHARACTER VARYING(4000)
);

CREATE TABLE t_plugin_travel_attr (
  withdata      CHARACTER(1)                NOT NULL,
  pk            INTEGER                     NOT NULL,
  createdat     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  createdby     CHARACTER VARYING(60)       NOT NULL,
  modifiedat    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  modifiedby    CHARACTER VARYING(60)       NOT NULL,
  updatecounter INTEGER                     NOT NULL,
  value         CHARACTER VARYING(3000),
  propertyname  CHARACTER VARYING(255)      NOT NULL,
  type          CHARACTER(1)                NOT NULL,
  parent        INTEGER                     NOT NULL
);

CREATE TABLE t_plugin_travel_attrdata (
  pk            INTEGER                     NOT NULL,
  createdat     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  createdby     CHARACTER VARYING(60)       NOT NULL,
  modifiedat    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  modifiedby    CHARACTER VARYING(60)       NOT NULL,
  updatecounter INTEGER                     NOT NULL,
  datacol       CHARACTER VARYING(2990),
  datarow       INTEGER                     NOT NULL,
  parent_id     INTEGER                     NOT NULL
);

ALTER TABLE t_plugin_travel
  ADD CONSTRAINT t_plugin_travel_pkey PRIMARY KEY (pk);

CREATE INDEX idx_fk_t_plugin_travel_kost2_id
  ON t_plugin_travel (kost2_id);

CREATE INDEX idx_fk_t_plugin_travel_employee_id
  ON t_plugin_travel (employee_id);

CREATE INDEX idx_fk_t_plugin_travel_tenant_id
  ON t_plugin_travel (tenant_id);

ALTER TABLE t_plugin_travel
  ADD CONSTRAINT fkklvgq7lo4uhtnew1bttn FOREIGN KEY (employee_id) REFERENCES t_fibu_employee (pk);

ALTER TABLE t_plugin_travel
  ADD CONSTRAINT fkknf08hlcvgzcgvfupohm FOREIGN KEY (kost2_id) REFERENCES t_fibu_kost2 (pk);

ALTER TABLE t_plugin_travel
  ADD CONSTRAINT fkt86thxrdu1i5r0f906a8 FOREIGN KEY (tenant_id) REFERENCES t_tenant (pk);

ALTER TABLE t_plugin_travel_attr
  ADD CONSTRAINT t_plugin_travel_attr_pkey PRIMARY KEY (pk);

ALTER TABLE t_plugin_travel_attr
  ADD CONSTRAINT fkp3rgw7jr876rx6vcaorg FOREIGN KEY (parent) REFERENCES t_plugin_travel (pk);

ALTER TABLE t_plugin_travel_attrdata
  ADD CONSTRAINT fko1hvirvkvxa3gp5kpcid FOREIGN KEY (parent_id) REFERENCES t_plugin_travel_attr (pk);
