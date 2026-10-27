# SC-D

El distintivo usa contarPendientesPorLote de SQLDelight, enlazando lote, labor y envio. Incluye creaciones, correcciones y eliminaciones pendientes. BaseLocal expone el Flow y el ViewModel solamente recoge sus valores; no incrementa un contador a mano. Al confirmar el servidor se borra el envío en la misma transacción y la consulta emite el valor actualizado.

La prueba contadorProvieneDeLaColaYSeLimpiaAlConfirmar verifica cero, uno al registrar sin envío y cero tras confirmar la sincronización.
