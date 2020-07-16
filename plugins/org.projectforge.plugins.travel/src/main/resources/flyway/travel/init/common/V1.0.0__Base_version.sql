-- This is the initial script for setting up the data base for this plugin.
-- For specific data base dialects, place sql scripts in the sub directory init/{vendor}

CREATE TABLE t_plugin_travel (
  pk                            INTEGER NOT NULL,
  created                       TIMESTAMP WITHOUT TIME ZONE,
  deleted                       BOOLEAN NOT NULL,
  last_update                   TIMESTAMP WITHOUT TIME ZONE,
  employee_id                   INTEGER NOT NULL,
  reason_of_travel              CHARACTER VARYING(4000),
  start_location                CHARACTER VARYING(30),
  return_location               CHARACTER VARYING(30),
  destination                   CHARACTER VARYING(255),
  kost2_id                      INTEGER,
  begin_of_travel               TIMESTAMP,
  end_of_travel                 TIMESTAMP,
  hotel                         BOOLEAN NOT NULL,
  rental_car                    BOOLEAN NOT NULL,
  train                         BOOLEAN NOT NULL,
  flight                        BOOLEAN NOT NULL,
  kilometers                    INTEGER,
  tenant_id                     INTEGER,
  value_string                  CHARACTER VARYING(100000),
  value_type                    CHARACTER VARYING(1000),
  attachments_names             CHARACTER VARYING(10000),
  attachments_ids               CHARACTER VARYING(10000),
  attachments_size              SMALLINT,
  attachments_last_user_action  CHARACTER VARYING(10000),
  assumption_of_costs           CHARACTER VARYING(4000),
  receipts_completely_available BOOLEAN NOT NULL
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
