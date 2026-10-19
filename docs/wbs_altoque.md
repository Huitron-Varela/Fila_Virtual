# Estructura de Desglose del Trabajo (WBS / EDT) - AlToque

## Nivel 1: Proyecto AlToque (Sistema de Filas Virtuales para Cafeterías)

---

## Nivel 2: Fases de Preparación y Módulos del Sistema

---

### Fase 1: Documentación, Metodología y Planificación

#### 1.1 Estructura General y Gestión del Proyecto
* Creación del esquema general del proyecto y estructura capitular de tesis.
* Elaboración y actualización continua del cronograma de actividades y desglose de trabajo.
* Definición de la metodología de desarrollo híbrida combinando Scrum y prototipado iterativo.

#### 1.2 Fundamentos del Proyecto (Capítulo 1)
* Redacción del contexto y antecedentes de las aglomeraciones en cafeterías universitarias.
* Planteamiento formal del problema operativo en el Campus El Naranjo de la Universidad de Colima.
* Justificación técnica, social y operativa de la solución móvil multiplataforma.
* Formulación del objetivo general y los objetivos específicos del proyecto.
* Planteamiento de la hipótesis general e hipótesis específicas de investigación.
* Delimitación formal del alcance del sistema y catálogo de limitaciones del proyecto.

#### 1.3 Marco Teórico y Estado del Arte (Capítulo 2)
* Fundamentación matemática en Teoría de Colas (disciplina de servicio FIFO y modelado mediante Ley de Little).
* Estudio conceptual de arquitecturas cliente-servidor distribuidas y el modelo Backend as a Service (BaaS).
* Análisis de bases de datos no relacionales orientadas a documentos con sincronización reactiva en tiempo real.
* Investigación del estado del arte mediante análisis comparativo de plataformas de gestión de filas y aplicaciones comerciales de alimentos.
* Normalización de referencias bibliográficas bajo el estándar internacional APA en su séptima edición.

#### 1.4 Metodología y Arquitectura del Sistema (Capítulo 3)
* Definición de los ciclos ágiles de trabajo (Sprints, ceremonias, backlog priorizado y entregables incrementales).
* Diseño conceptual y lógico de la estructura de base de datos no relacional (colecciones y asociaciones de datos).
* Elaboración de la tabla y catálogo de especificación de servicios web, endpoints y APIs del sistema.
* Definición del modelo de control de versiones y ramas colaborativas mediante GitFlow.

#### 1.5 Análisis del Sistema y Casos de Uso (Capítulo 4)
* Identificación, caracterización y matriz de permisos de los cuatro actores del sistema: Cliente, Empleado Operativo, Gerente de Cafetería y Administrador General (CEO).
* Especificación formal de los requerimientos funcionales del sistema, detallando entradas, procesos y salidas.
* Especificación de los requerimientos no funcionales (rendimiento, latencia menor a 500 ms, soporte de usuarios concurrentes, disponibilidad y seguridad).
* Elaboración de diagramas de casos de uso estructurados por cada rol del sistema.
* Elaboración de diagramas de flujo de trabajo para compras del cliente, administración de personal, catálogo de menú y gestión de establecimientos.
* Elaboración del diagrama de secuencia para el flujo de solicitud de turno en tiempo real con pago verificado.

---

### Fase 2: Diseño y Prototipado de Experiencia de Usuario (UI/UX)

#### 2.1 Diseño Conceptual y Flujos de Navegación
* Elaboración de bocetos y mockups de baja fidelidad para el recorrido de compra y consulta del estudiante.
* Mapeo de navegación e interfaces de trabajo para los empleados en las estaciones de cocina, entrega y caja.
* Diseño de interfaces de control para la administración gerencial y supervisión general de locales.

#### 2.2 Prototipado Visual de Alta Fidelidad
* Construcción de prototipos interactivos aplicando una paleta de colores semántica institucional y contrastes visuales accesibles.
* Creación de un sistema de diseño consistente con botones primarios y secundarios, campos de texto con validación y distintivos de estado.
* Diseño de elementos visuales de retroalimentación inmediata, tales como pantallas esqueleto para carga de datos, barras de avance y alertas flotantes.
* Validación del diseño mediante pruebas de usabilidad y recorrido visual con usuarios muestra.

---

### Fase 3: Módulo de Autenticación, Seguridad y Perfil de Usuario

#### 3.1 Diseño Frontend (Vistas de Acceso y Registro)
* Maquetación de la pantalla de inicio de sesión con campos para correo electrónico, contraseña con opción para alternar visibilidad, botón de acceso, botón de inicio de sesión con Google y enlace para recuperación de contraseña.
* Maquetación de la pantalla de registro con formulario de captura para nombre completo, número de teléfono, correo electrónico, contraseña y confirmación de contraseña.
* Integración de un contenedor con animación suave para alternar de forma interactiva entre las vistas de inicio de sesión y registro.
* Maquetación de la interfaz para ingreso de código de seguridad de seis dígitos con casillas numéricas individuales independientes.
* Validaciones visuales en tiempo real para verificar el formato de correo electrónico, longitud de número telefónico y nivel de seguridad de la contraseña.
* Inclusión de casilla de verificación obligatoria para la lectura y aceptación de los Términos y Condiciones y Aviso de Privacidad.
* Maquetación de la pantalla de perfil con tarjeta de datos personales, avatar del usuario, estado de verificación de cuenta y opciones de configuración.
* Maquetación de la pantalla de edición de perfil con campos editables para actualizar nombre, teléfono y selector de fotografía desde la galería del dispositivo.

#### 3.2 Desarrollo Backend (Lógica de Autenticación y Conexión de Datos)
* Configuración e inicialización del servicio de autenticación en la nube para la plataforma móvil.
* Programación del flujo de autenticación federada con cuentas de Google, sincronizando los datos básicos del usuario.
* Lógica de registro e inicio de sesión seguro con correo electrónico y contraseña bajo estándares de cifrado.
* Generación de código de verificación temporal de seis dígitos y persistencia con marca de tiempo en la base de datos.
* Despacho automatizado de correo electrónico corporativo con plantilla visual que entrega el código de verificación para validar la identidad del usuario.
* Lógica de verificación que contrasta el código ingresado con el valor almacenado en la base de datos para habilitar la cuenta.
* Flujo de recuperación de acceso que permite restablecer la contraseña previa validación del código enviado al correo.
* Lógica para consultar, renderizar y actualizar la información del perfil del usuario en la base de datos.
* Procesamiento y almacenamiento en la nube de la nueva fotografía de perfil del usuario con generación de enlace público.
* Lógica de cierre de sesión seguro que invalida las credenciales activas en el dispositivo y limpia el estado local.

---

### Fase 4: Módulo del Cliente (Exploración, Menú y Carrito de Compras)

#### 4.1 Diseño Frontend (Vistas de Compra y Selección de Alimentos)
* Maquetación de la pantalla principal con barra de búsqueda, carrusel de categorías de comida (platillos, bebidas, botanas) y listado de cafeterías disponibles.
* Diseño de tarjetas informativas de establecimientos que muestran logotipo, nombre, descripción corta, horario de servicio, distintivo de local abierto o cerrado y calificación promedio con estrellas.
* Maquetación de la pantalla del menú de la cafetería seleccionada con pestañas de filtrado por categoría y buscador interno de alimentos.
* Diseño de tarjetas de productos que muestran fotografía, título del platillo, descripción de ingredientes, precio en moneda nacional y botón de agregar.
* Maquetación de la ventana modal de detalle del producto con selector de cantidad numérica y botón para incorporar los artículos al pedido.
* Indicador visual de bloqueo y distintivo de agotado temporalmente en platillos que no cuentan con disponibilidad en el momento.
* Maquetación de la pantalla del carrito de compras con lista de artículos seleccionados, controles para incrementar, disminuir o eliminar platillos, y desglose de costos (subtotal, tarifa de servicio y total a pagar).

#### 4.2 Desarrollo Backend (Lógica de Catálogo, Filtros y Gestión del Carrito)
* Programación de consultas reactivas a la base de datos para cargar en tiempo real las cafeterías activas registradas en el sistema.
* Lógica de consulta en tiempo real para extraer los productos y precios vigentes asociados exclusivamente a la cafetería que el usuario seleccionó.
* Gestión en memoria del estado del carrito de compras con control de cantidades y persistencia temporal durante la navegación.
* Implementación de la regla de negocio que restringe que el carrito contenga únicamente productos de un solo establecimiento por pedido.
* Algoritmo de cálculo de importes para obtener subtotales, aplicar la tarifa de servicio de plataforma y calcular el monto final a pagar.
* Verificación de consistencia y disponibilidad en la base de datos antes de proceder al cobro para evitar la compra de productos agotados.

---

### Fase 5: Pasarela de Pagos y Billetera Digital

#### 5.1 Diseño Frontend (Vistas de Billetera y Pasarela de Pago)
* Maquetación de la pantalla de billetera digital con visualización de tarjetas registradas por el usuario, mostrando logotipo del emisor bancario (Visa, Mastercard, American Express), los últimos cuatro dígitos y la fecha de vigencia.
* Formulario seguro de captura de tarjeta con detección gráfica automática del emisor según los números ingresados, campos para número de plástico, nombre del titular, fecha de expiración y código de seguridad (CVV).
* Pantalla de confirmación y espera interactiva que acompaña el proceso de redirección y regreso seguro desde la pasarela de pagos externa.

#### 5.2 Desarrollo Backend (Lógica de Tokenización, Cobro y Verificación)
* Conexión mediante cliente de red con la pasarela de pagos de Mercado Pago para generar la orden y preferencia de cobro.
* Configuración de enlaces profundos (deep links) para redirigir al usuario al entorno web de pago y garantizar su retorno automático a la aplicación móvil.
* Tokenización segura de los datos bancarios directamente con los servicios de la pasarela para evitar el almacenamiento de información sensible en servidores propios.
* Almacenamiento seguro en la base de datos del identificador de token de pago, últimos cuatro dígitos, marca y nombre del titular, omitiendo estrictamente el código de seguridad (CVV).
* Programación de función segura en el servidor en la nube (Cloud Function) que recibe la solicitud de compra, verifica la identidad del cliente y procesa el cobro con la pasarela.
* Manejo estructurado de estados de pago: confirmación de transacciones aprobadas, marcado de transacciones pendientes y control de reintentos ante rechazos bancarios sin generar órdenes duplicadas.

---

### Fase 6: Fila Virtual, Gestión de Pedidos y Turnos en Tiempo Real

#### 6.1 Diseño Frontend (Pantalla de Órdenes, Turnos y Código QR)
* Maquetación de la vista de actividades dividida en dos secciones: seguimiento del pedido actual y consulta del historial de compras previas.
* Tarjeta de monitoreo del turno en curso que muestra el número asignado, la posición actual en la fila, el tiempo estimado de espera y el estado operativo del pedido (Recibido, En Preparación, Listo).
* Desbloqueo y renderizado de un código QR único en pantalla acompañado de un código alfanumérico legible de cuatro caracteres cuando la orden pasa a estado "Listo".
* Ventana modal de retroalimentación que permite calificar la experiencia mediante un sistema de cinco estrellas y caja de comentarios una vez entregado el pedido.

#### 6.2 Desarrollo Backend (Motor de Turnos FIFO, Sincronización y Reseñas)
* Creación del registro formal del pedido en la base de datos únicamente después de verificar la aprobación del pago.
* Algoritmo de asignación de turnos virtuales que genera un número secuencial para el cliente respetando el orden cronológico de llegada (disciplina FIFO).
* Algoritmo de dispersión (hash) que genera un código alfanumérico de cuatro caracteres único y seguro asociado al pedido para su recogida.
* Conexión de escuchadores reactivos en tiempo real con la base de datos para que los cambios de estado aplicados por la cocina se reflejen al instante en la pantalla del cliente.
* Persistencia de las evaluaciones emitidas por los usuarios y cálculo automático del promedio de calificación y popularidad de los establecimientos.

---

### Fase 7: Módulo de Administración Global (Panel del Administrador General / CEO)

#### 7.1 Diseño Frontend (Vistas de Administración General)
* Maquetación del panel principal de administración con resumen general de cafeterías registradas y accesos directos de control.
* Pantalla de listado de cafeterías con buscador por nombre, tarjetas informativas de cada negocio e interruptores para habilitar o suspender cada local.
* Formulario para registrar y editar cafeterías con campos para nombre comercial, descripción, dirección física, categorías, editor de horarios diarios y selector de logotipo.
* Ventana modal de confirmación previa para suspender o retirar establecimientos del catálogo.

#### 7.2 Desarrollo Backend (Lógica de Gestión de Locales)
* Control de acceso que restringe las acciones administrativas exclusivamente a usuarios con rol de administrador general verificado en la base de datos.
* Operaciones de base de datos para registrar nuevas cafeterías, actualizar su información y modificar su estado operativo (abierto, cerrado o suspendido).
* Procesamiento y almacenamiento en la nube de los archivos de logotipo con generación de enlaces públicos permanentes.
* Lógica de baja lógica para inhabilitar un establecimiento de la vista del cliente sin eliminar su historial de ventas, pedidos ni métricas operativas.

---

### Fase 8: Módulo de Gestión de Menú y Catálogo (Gerente de Cafetería)

#### 8.1 Diseño Frontend (Vistas de Administración del Menú)
* Maquetación del catálogo de productos del establecimiento con filtrado por categoría (Comida, Bebidas, Snacks) y buscador en vivo.
* Tarjetas de platillos que muestran fotografía, nombre, precio, descripción y un interruptor para alternar la disponibilidad para compra.
* Formulario para dar de alta o modificar productos con campos de título, descripción, costo, categoría y selector de imagen desde la galería.
* Modal de confirmación previa a la eliminación de platillos del menú.

#### 8.2 Desarrollo Backend (Lógica de Gestión de Productos)
* Verificación de permisos para asegurar que el gerente únicamente pueda consultar y manipular los productos de su propio establecimiento.
* Operaciones de base de datos para registrar nuevos platillos, bebidas y botanas vinculándolos al identificador de la cafetería.
* Procesamiento y carga de fotografías de alimentos en el almacenamiento en la nube con generación de direcciones web para su visualización.
* Operaciones para actualizar precios, nombres, descripciones y disponibilidad de platillos en tiempo real.
* Eliminación controlada de productos garantizando que las órdenes históricas que los incluyeron conserven sus datos intactos.

---

### Fase 9: Módulo de Personal y Gestión de Colaboradores

#### 9.1 Diseño Frontend (Vistas de Gestión de Empleados e Invitaciones)
* Maquetación de la lista de empleados asignados a la cafetería, mostrando nombre, correo, fotografía, rol desempeñado y botón de desvinculación.
* Formulario para registrar o invitar nuevos colaboradores mediante correo electrónico, con asignación de funciones (Gerente, Cocina, Entrega, Cajero).
* Tarjeta informativa visual que presenta el enlace directo o código de invitación para compartir con el personal.
* Página web receptora responsiva para recepción de enlaces de invitación y redirección hacia la aplicación móvil.
* Pantalla dentro de la aplicación móvil para que el empleado confirme su identidad y acepte su incorporación al equipo de trabajo.

#### 9.2 Desarrollo Backend (Lógica de Invitaciones, Seguridad y Asignación de Roles)
* Generación de tokens criptográficos temporales con vigencia de 24 horas asociados a la invitación de personal.
* Creación y despacho automático de correos electrónicos con formato profesional que entregan el enlace de aceptación mediante el servicio de mensajería en la nube.
* Configuración de enlaces profundos para que al abrir la invitación desde el navegador web se enlace directamente con la pantalla correspondiente en la aplicación móvil.
* Validación de identidad del usuario invitado y actualización de sus permisos y roles laborales en la base de datos.
* Lógica para revocar la asignación de un empleado en un establecimiento sin dar de baja su cuenta personal en la plataforma.

---

### Fase 10: Módulo de Estaciones Operativas del Empleado

#### 10.1 Diseño Frontend (Interfaces de Estaciones de Trabajo)
* Pantalla de inicio de jornada laboral donde el empleado consulta su cafetería asignada y selecciona la estación de trabajo a operar (Cocina, Entrega o Cajero).
* Interfaz de la Estación de Cocina que muestra pedidos en estado recibido y en preparación, con panel resumen de platillos agrupados para cocinar en tandas y botones para avanzar el estado.
* Interfaz de la Estación de Entrega que lista pedidos listos, botón para abrir la cámara del dispositivo con escáner de códigos QR y botón para marcar pedidos como no reclamados.
* Interfaz de la Estación de Cajero para supervisión general del flujo de órdenes, verificación de pagos e historial para resolver dudas o incidencias con los clientes.
* Pantalla de contingencia rápida para marcar ingredientes o platillos como agotados temporalmente sin necesidad de entrar a editar el catálogo general.
* Pantalla de resumen de jornada laboral al cerrar turno, presentando el tiempo trabajado y la cantidad de órdenes elaboradas o entregadas.

#### 10.2 Desarrollo Backend (Lógica Operativa de Estaciones y Métricas)
* Registro de sesión de trabajo al abrir turno con identificador del empleado, establecimiento, estación seleccionada y hora de inicio.
* Lógica de transición de estados del pedido en la base de datos (de Recibido a En Preparación y de En Preparación a Listo) registrando al empleado responsable de la acción.
* Lógica de lectura y validación instantánea del código QR escaneado con la cámara, verificando que pertenezca al local y esté listo antes de marcarlo como Entregado.
* Lógica de marcado como No Reclamado cuando el cliente no acude por su pedido tras superar el tiempo límite, finalizando la orden sin generar reembolsos.
* Lógica de agotado temporal que actualiza de inmediato el producto en la base de datos y lo oculta del menú del cliente para impedir nuevas compras.
* Lógica de cierre de turno y cálculo acumulado de métricas operativas por jornada (pedidos preparados, entregados y abandonados) almacenadas en la base de datos para auditoría gerencial.

---

### Fase 11: Revisión de Diseño, Responsividad y Experiencia Visual

#### 11.1 Auditoría Visual y Ajustes de Responsividad
* Revisión integral del diseño en todas las pantallas de la aplicación para asegurar consistencia visual, jerarquía tipográfica y apego a la paleta de colores institucional.
* Implementación de reglas de diseño responsivo adaptativo que escalan dimensiones, espaciados y tipografías dinámicamente según el tamaño del dispositivo (teléfonos compactos, estándar y tabletas).
* Adaptación de cabeceras de cafeterías, tarjetas de platillos, carruseles y barras de navegación para prevenir desbordamientos o cortes de contenido en pantallas de distintas resoluciones.
* Estandarización de componentes visuales de retroalimentación inmediata, integrando pantallas esqueleto animadas (skeleton loaders) durante la carga asíncrona de datos para evitar transiciones bruscas.
* Integración de alertas visuales flotantes (toasts) que confirman de manera amigable acciones exitosas o advierten sobre posibles errores operativos.
* Revisión y maquetación de las pantallas informativas de soporte y marco legal (Términos y Condiciones, Aviso de Privacidad y Preguntas Frecuentes).

#### 11.2 Soporte de Internacionalización y Ajustes de Lenguaje
* Revisión e implementación de soporte bilingüe en toda la interfaz para alternar dinámicamente entre Español e Inglés desde el perfil de usuario sin reiniciar la aplicación.
* Centralización de recursos de texto y mensajes del sistema para garantizar coherencia en la traducción y redacción de cada módulo.

---

### Fase 12: Aseguramiento de Calidad y Pruebas (Despliegue Pendiente)

#### 12.1 Pruebas y Verificación del Sistema (Completado)
* Pruebas unitarias sobre funciones de cálculo (costos del carrito, algoritmos de asignación de turnos y validaciones de formularios).
* Pruebas de integración de la comunicación reactiva entre la aplicación del cliente y las estaciones de cocina y entrega mediante la base de datos en tiempo real.
* Pruebas de transacciones en entorno de desarrollo con la pasarela de pagos para simular compras aprobadas, pendientes y rechazadas.
* Pruebas de escaneo y lectura del código QR con la cámara del dispositivo bajo distintas condiciones de iluminación.

#### 12.2 Seguridad y Reglas de la Base de Datos (Completado)
* Implementación y auditoría de reglas de seguridad en la nube que restringen la lectura y escritura estrictamente según el rol autenticado de cada usuario.
* Bloqueo de modificaciones directas en precios, estados de pago o asignación de turnos desde el lado del cliente móvil.

#### 12.3 Pruebas de Campo, Piloto y Despliegue en Producción (Pendiente)
* Ejecución de pruebas piloto en escenario real de operación en la cafetería del Campus El Naranjo de la Universidad de Colima durante horarios de receso escolar (Pendiente).
* Pruebas de estrés y concurrencia simulando alta demanda de pedidos y solicitudes simultáneas de turnos (Pendiente).
* Generación de paquetes ejecutables para dispositivos Android (APK/AAB) y preparación para compilación en iOS para su distribución final (Pendiente).
* Puesta en marcha oficial y despliegue del sistema en producción (Pendiente).