# Portable Format for Analytics Nodes for KNIME
KNIME nodes for reading and executing Portable Format for Analytics (PFA) scoring engines.

The [Portable Format for Analytics](http://dmg.org/pfa/index.html) is a
JSON-based document format for describing scoring engines.

[KNIME Analytics Platform](https://www.knime.com) is an open source data analytics software that allows its users to
build analytics workflows visually by connecting data processing nodes.

This repository contains code for two KNIME nodes: a PFA Reader and a PFA Predictor.
The PFA Reader reads PFA documents in YAML or JSON format and provides it as a custom KNIME port object.
The PFA Predictor takes such an imported PFA file and a KNIME data table and executes the scoring engine on the data.

Mete Can Acar ([Metete](https://github.com/Metete)) contributed code to this project.
