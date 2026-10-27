# SC-C

El campo cantidadJornales recorre DTO, conversión al dominio, SQLDelight, repositorio y formulario. Se muestra también en cada labor del detalle. Los errores del servidor aparecen debajo del campo. La migración 1.sqm agrega la columna con cero por defecto y conserva las labores anteriores.

Las pruebas verifican el recorrido del dato y la migración de una fila anterior. El backend rechaza jornales negativos con 400 y validationErrors.cantidadJornales.
