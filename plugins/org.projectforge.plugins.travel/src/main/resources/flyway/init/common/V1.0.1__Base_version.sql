ALTER TABLE t_plugin_travel
  ADD tenant_id INTEGER;

CREATE INDEX idx_fk_t_plugin_travel_tenant_id
  ON t_plugin_travel (tenant_id);

ALTER TABLE t_plugin_travel
  ADD CONSTRAINT fkt86thxrdu1i5r0f906a8 FOREIGN KEY (tenant_id) REFERENCES t_tenant (pk);
