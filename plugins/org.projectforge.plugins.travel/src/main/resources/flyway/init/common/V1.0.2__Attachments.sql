ALTER TABLE t_plugin_travel ADD COLUMN attachments_names CHARACTER VARYING(10000);
ALTER TABLE t_plugin_travel ADD COLUMN attachments_ids CHARACTER VARYING(10000);
ALTER TABLE t_plugin_travel ADD COLUMN attachments_size SMALLINT;
ALTER TABLE t_plugin_travel ADD COLUMN attachments_last_user_action CHARACTER VARYING(10000);
ALTER TABLE t_plugin_travel ADD COLUMN assumption_of_costs CHARACTER VARYING(4000);
