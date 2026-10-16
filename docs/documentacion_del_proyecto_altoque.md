UNIVERSIDAD DE COLIMA
FACULTAD DE INGENIERÍA ELECTROMECÁNICA
Análisis y diseño de una aplicación para filas virtuales en cafeterías
Carrera
Ingeniería de Software
Semestre y Grupo
6E
Presenta (n)
Gutiérrez Rúa Gerardo Adonai
Huitrón Varela Miguel Angel
Sinodal
Mtro. Ballinas Arteaga Jesús Emilio

Contenido
Introducción 4
Capítulo 1. Planteamiento del Problema 5
1.1 Contexto del problema 5
1.2 Planteamiento del problema 5
1.3 Justificación 6
1.4 Objetivo general 6
1.5 Objetivos específicos 6
1.6 Hipótesis 7
1.6.1 Hipótesis General 7
1.6.2 Hipótesis Específicas 7
1.7 Alcances del proyecto 7
1.8 Limitaciones 8
Capítulo 2. Marco Teórico 8
2.1 Sistemas de Información Basados en Turnos 8
2.2 Desarrollo de Aplicaciones Móviles Multiplataforma 8
2.3 Arquitectura de Software Distribuida 9
2.3.1 El Modelo Cliente-Servidor en Entornos Móviles 9
2.3.2 Arquitectura Backend as a Service (BaaS) 9
2.4 Sistemas de Gestión de Bases de Datos NoSQL 9
2.4.1 Estructura de Base de Datos: 10
2.5 Tecnologías Utilizadas en el Proyecto 10
2.5.1 Kotlin Multiplatform 10
2.5.2 Firebase (Auth y Cloud Messaging) 11
2.5.3 Cloud Firestore 11
2.5.4 Control de Versiones (Git + GitHub) 12
2.6 Modelo y Teoría de Gestión de Colas seleccionado. 12
2.7 Proyectos Similares y Estado del Arte 12
Plataformas de Gestión de Filas y Turnos 13
Soluciones en el Contexto Latinoamericano 13
Soluciones Empresariales (Hardware y Software) 13
Estado del Arte: Proyectos Académicos 13
Referentes de Modelo de Negocio (Delivery) 13
Capítulo 3. Metodología 14
3.1 Tipo de investigación 14
3.2 Metodología de Desarrollo Híbrida (Scrum y Prototipado Iterativo) 14
3.3 Técnicas de Recolección de Requerimientos 15
3.4 Análisis, Diseño y Pruebas del Sistema 15
Capítulo 4. Análisis del Sistema 16
4.1 Identificación de Actores del Sistema 16
4.2 Requerimientos Funcionales 16
4.3 Requerimientos No Funcionales 17
4.4 Diagramas de Casos de Uso 18
4.4.1. Diagrama de Casos de Uso: Usuario Cliente (App Móvil) 18
4.4.2. Diagrama de Casos de Uso: Cafetería (Operativo y Gerencial) 18
4.4.3 Diagrama de Secuencia: Flujo de Solicitar un Turno en Tiempo Real 19
4.4.4 Casos de Uso: Administrador General (CEO) 20
Referencias 21

Tabla de Ilustración
Ilustración 1 diseño de la base de datos NoSQL de la aplicación AlToque 14
Ilustración 2 Diagrama caso de uso para el rol de cliente 18
Ilustración 3 Diagrama Caso de Uso Rol Gerente 19
Ilustración 4 Descripción de Administrador General CEO 21
Ilustración 5 Empleado Operativo Funciones Comunes 21
Ilustración 6 Empleado Operativo Estacion Cocina 22
Ilustración 7 Empleado Operativo Estacion Entrega 23
Ilustración 8 Empleado Operativo Estacion Cajero 24
Ilustración 9 Fluo Cliente 25
Ilustración 10 CRUD Empleados 27
Ilustración 11 CRUD Menu 29
Ilustración 12 CRUD Establecimientos 31
Ilustración 13 Diagrama Secuencia: Flujo Solicitar Turno en Tiempo Real 32

# Introducción

El presente documento constituye la documentación técnica del proyecto Análisis y diseño de una aplicación para filas virtuales en cafeterías, una aplicación móvil multiplataforma diseñada para la gestión de filas virtuales en establecimientos de alimentos y bebidas de la Universidad de Colima. Este proyecto surge como respuesta a una problemática latente en el entorno universitario: la ineficiencia en la gestión de filas durante los horarios de alta demanda, lo cual afecta tanto la experiencia de los usuarios como la operatividad de los establecimientos de servicio.
En la era digital actual, las soluciones tecnológicas han transformado diversos aspectos de la vida cotidiana, desde la manera en que nos transportamos hasta la forma en que realizamos compras y gestionamos nuestro tiempo. Aplicaciones como Uber, Didi y diversos sistemas de reservaciones han demostrado que la digitalización de procesos tradicionales puede generar beneficios significativos en términos de eficiencia, comodidad y experiencia del usuario. Sin embargo, el ámbito de la gestión de filas en entornos universitarios ha permanecido relativamente inalterado, representando una oportunidad de innovación con impacto directo en la calidad de vida de la comunidad estudiantil.
El proyecto de Análisis y diseño de una aplicación para filas virtuales en cafeterías se desarrolla en el marco de la carrera de Ingeniería de Software de la Universidad de Colima, bajo la metodología SCRUM combinada con técnicas de prototipado iterativo. La solución propuesta utiliza tecnologías modernas como Kotlin Multiplatform para el desarrollo frontend y Firebase como plataforma backend, lo cual permite crear una aplicación nativa para iOS y Android con capacidades de sincronización en tiempo real, autenticación múltiple y notificaciones push.
Este documento presenta la estructura completa del proyecto, comenzando con los antecedentes que contextualizan la problemática, seguido del planteamiento del problema y su justificación. Posteriormente, se detallan los objetivos que guían el desarrollo, las hipótesis de investigación y el alcance del proyecto. El marco teórico fundamenta los aspectos técnicos y conceptuales, mientras que los capítulos posteriores abordan la metodología de desarrollo, el análisis de requisitos y el diseño del sistema. Finalmente, se incluyen las referencias bibliográficas que sustentan el contenido del presente trabajo.
La importancia de este proyecto trasciende el ámbito académico, pues representa una solución con potencial de escalabilidad hacia otros contextos institucionales y comerciales. La implementación exitosa de Investigación de Filas Virtuales en Cafeterías podría servir como modelo para la modernización de procesos de gestión de filas en diversos escenarios, desde instituciones educativas hasta establecimientos de servicio al público en general, contribuyendo a la optimización de tiempos y recursos tanto de usuarios como de prestadores de servicios.

# Capítulo 1. Planteamiento del Problema

## 1.1 Contexto del problema

La gestión de filas de espera sigue siendo un reto importante en entornos con alta concurrencia, como cafeterías universitarias, establecimientos de comida rápida y oficinas de servicio al público. Históricamente, los usuarios se han visto obligados a formarse en filas extensas, lo cual provoca una pérdida considerable de tiempo, frustración y una mala experiencia en general. Esta problemática se intensifica en el ámbito universitario, donde los recesos son cortos y la demanda de alimentos se concentra en horarios muy específicos. Esto genera una saturación de personas que limita el acceso rápido a los servicios para estudiantes, docentes y personal administrativo.
En la Universidad de Colima, este problema es evidente durante los recesos, momento en el que todos los establecimientos de comida se congestionan de manera significativa. El alumnado y el personal enfrentan constantemente la difícil decisión de formarse en largas filas que consumen gran parte de su descanso, o simplemente evitar comprar alimentos durante esos periodos. Esta situación no solo afecta negativamente la experiencia del usuario, sino que también genera problemas operativos para las cafeterías, ya que su capacidad para atender a todos los clientes se ve limitada en las horas de máxima demanda.
Afortunadamente, los avances tecnológicos recientes han permitido crear soluciones digitales innovadoras para problemas cotidianos. Aplicaciones como Didi, Uber y distintos sistemas de reservaciones han cambiado la forma en que los usuarios interactúan con los servicios tradicionales, demostrando que es posible optimizar estos procesos mediante la tecnología. En este contexto, surge la necesidad de desarrollar un sistema que resuelva de manera específica el problema de las filas de espera en las instalaciones universitarias, adaptando los modelos tecnológicos que ya han tenido éxito en otras industrias a las necesidades propias del entorno educativo.

## 1.2 Planteamiento del problema

En el área de servicios estudiantiles de la Universidad de Colima, Campus El Naranjo, se presenta una alta congestión en el local de venta de alimentos durante los periodos de receso. Esta situación se origina por una combinación de factores logísticos y operativos: la salida simultánea de múltiples grupos de estudiantes, la capacidad limitada del local disponible y los tiempos prolongados tanto en la toma de pedidos variados como en la preparación de los mismos. Como resultado, se generan filas excesivas y tiempos de espera prolongados, lo que afecta negativamente la experiencia de los estudiantes, quienes pierden la mayor parte de su tiempo de descanso intentando adquirir sus alimentos.

## pregunta de investigación

¿De qué manera el desarrollo de una aplicación móvil multiplataforma para la gestión remota de turnos incidirá en la eficiencia operativa y en la experiencia de servicio de los estudiantes en la cafetería de la Universidad de Colima, Campus El Naranjo?

## 1.3 Justificación

El desarrollo del proyecto Análisis y diseño de una aplicación para filas virtuales en cafeterías se justifica en tres áreas principales: su impacto social, su viabilidad técnica y su potencial comercial.
Desde el punto de vista social, implementar un sistema de filas virtuales mejorará el bienestar de la comunidad universitaria al eliminar los tiempos de espera innecesarios, reducir el estrés causado por la alta concentración de personas y mejorar la experiencia de los usuarios durante sus recesos. Estos cambios benefician la rutina diaria de estudiantes y trabajadores, contribuyendo a un ambiente escolar más organizado y satisfactorio.
En el aspecto técnico, el proyecto es una excelente oportunidad para aplicar conocimientos en el desarrollo de aplicaciones móviles multiplataforma, arquitectura de sistemas, gestión de bases de datos en tiempo real y diseño de interfaces centradas en el usuario. El uso de Kotlin MultiPlatform permite crear una aplicación nativa para iOS y Android compartiendo la misma lógica de programación, lo cual es una forma moderna y muy eficiente de desarrollar software. Además, el uso de Firebase proporciona la sincronización en tiempo real necesaria para administrar los turnos, facilitando el manejo de cuentas de usuario, el almacenamiento de datos y el envío de notificaciones a los dispositivos.
Finalmente, el proyecto tiene un gran potencial comercial. La aplicación no solo funciona para el entorno universitario, sino que puede adaptarse y escalar a otros lugares donde la organización de filas es un problema, como restaurantes, bancos, consultorios médicos u oficinas públicas. Al utilizar un modelo de negocio similar al de aplicaciones como Uber o Didi, pero enfocado en la gestión de turnos, se abre una valiosa oportunidad de innovación en un mercado que aún tiene muchas necesidades por resolver.

## 1.4 Objetivo general

Desarrollar una aplicación móvil multiplataforma, que permita a los usuarios de la Universidad de Colima Campus Naranjo gestionar a distancia sus turnos en la cafetería, disminuyendo el tiempo de espera fila físicamente y mejorando tanto la experiencia del cliente como la eficiencia operativa de los establecimientos.

## 1.5 Objetivos específicos

Integrar un sistema de autenticación robusto que permita el registro y acceso seguro de usuarios mediante múltiples proveedores (Google, Apple), incorporando un proceso de verificación de identidad para prevenir fraudes.
Diseñar un módulo integral de gestión de turnos que habilite a los usuarios para solicitar, visualizar de manera remota, desplegando información en tiempo real sobre su posición en la fila y el tiempo de espera estimado.
Construir un panel de administración enfocado en el concesionario del establecimiento, el cual facilite la gestión centralizada de menús, precios, categorías de productos y controle el flujo de atención mediante un esquema de roles diferenciados.
Implementar un mecanismo de generación y validación de códigos QR que garantice la correcta identificación de los usuarios y agilice la entrega de los alimentos de manera exacta y sin errores.

## 1.6 Hipótesis

### 1.6.1 Hipótesis General

La implementación de la aplicación móvil multiplataforma AlToque para la gestión de filas virtuales en las cafeterías de la Universidad de Colima reducirá significativamente el tiempo de espera físico de los usuarios y mejorará la eficiencia operativa de los establecimientos, en comparación con el sistema tradicional de filas.

### 1.6.2 Hipótesis Específicas

La digitalización del proceso de turnos permitirá a los usuarios reducir su tiempo de espera físico en al menos un 60%, brindándoles la libertad de realizar otras actividades durante sus recesos mientras aguardan de manera virtual.
El uso de notificaciones push incrementará la puntualidad de los clientes al momento de recoger su pedido, disminuyendo la tasa de abandono de filas que sufren actualmente los establecimientos.
La integración de roles de acceso diferenciados optimizará la administración interna de las cafeterías, facilitando una distribución más eficiente de las tareas y reduciendo el estrés del personal en horas pico.
El uso de la tecnología Kotlin MultiPlatform permitirá obtener un rendimiento técnico comparable al de aplicaciones nativas, reduciendo los tiempos y costos de desarrollo frente a la creación de soluciones independientes para iOS y Android.

## 1.7 Alcances del proyecto

El alcance de este proyecto incluye el desarrollo completo de la aplicación móvil multiplataforma AlToque (para iOS y Android), enfocada en la gestión de filas virtuales para la venta de alimentos y bebidas. Aunque el sistema está diseñado inicialmente para las cafeterías de la Universidad de Colima, cuenta con la arquitectura y escalabilidad necesarias para implementarse en el futuro en otras instituciones educativas o negocios comerciales.
En su versión inicial, el sistema integrará las siguientes funcionalidades principales:
Gestión de usuarios: Sistema de registro y acceso mediante múltiples métodos (Google, Apple y número de teléfono), incluyendo una validación de identidad con documento oficial.
Gestión de turnos en tiempo real: Módulo para la asignación y el seguimiento remoto de turnos asociados a pedidos con pago confirmado. Permitirá consultar la posición en la fila, el tiempo de espera estimado y el estado del pedido durante su preparación y entrega. Una vez confirmado el pago, el sistema no permitirá cancelar el pedido.
Notificaciones: Alertas push para mantener informado al usuario sobre el estado de su turno.
Módulo de administración de establecimientos: Panel de control con acceso basado en roles diferenciados (CEO, Gerente y Empleado).
Catálogo de productos: Visualización de menús organizados por categorías (comidas, bebidas, botanas) con la funcionalidad de carrito de compras integrado.
Pago mediante Mercado Pago Checkout Pro: Integración que permitirá al cliente completar el pago en el entorno de Mercado Pago y regresar a AlToque para consultar el resultado y dar seguimiento a su pedido. El sistema verificará el estado de la transacción mediante el backend antes de confirmar el pedido y asignar el turno. AlToque conservará la referencia de la transacción, el importe y el estado del pago necesarios para el control operativo, sin incorporar formularios propios para registrar tarjetas.
Códigos QR: Generación de un código QR único para cada pedido con pago confirmado, que el cliente deberá presentar al recoger sus productos. El empleado de la estación de Entrega escaneará el código para verificar que corresponda al establecimiento y a un pedido en estado “Listo”, antes de confirmar su entrega. El sistema impedirá utilizar nuevamente el código para registrar una segunda entrega del mismo pedido.
Calificaciones y reseñas: Sistema para que los usuarios puedan evaluar y dejar comentarios sobre los establecimientos.

## 1.8 Limitaciones

Para esta primera versión del sistema, no se incluirán las siguientes funcionalidades, las cuales quedan contempladas para futuras actualizaciones de la aplicación:
Pago integrado y Wallet interna: La primera versión no incluirá la integración de Mercado Pago Checkout API ni una Wallet propia para registrar, administrar o seleccionar tarjetas dentro de AlToque. Estas funcionalidades se contemplan como mejoras futuras. El alcance inicial incluirá únicamente el pago mediante redirección al entorno de Mercado Pago a través de Checkout Pro. Tampoco se incluirá la integración con terminales bancarias físicas.
Estadísticas avanzadas: No se incluirá un módulo de análisis predictivo para anticipar la demanda de los establecimientos.
Sistema de fidelización: No habrá programas de lealtad, puntos o recompensas para los clientes frecuentes.
Integración académica: La aplicación no estará vinculada con los sistemas de control escolar o bases de datos internas de la universidad.
Pedidos programados: No se habilitará la opción para que los usuarios programen pedidos con horas o días de anticipación.
Redes sociales: No se incluirán funcionalidades nativas para compartir compras o perfiles en redes sociales.

# Capítulo 2. Marco Teórico

## 2.1 Sistemas de Información Basados en Turnos

Los sistemas de colas virtuales constituyen la evolución digital de la organización de filas físicas de espera, sustentándose en los principios formalizados por la Teoría de Colas originada por Agner Krarup Erlang a principios del siglo XX (Gross et al., 2018). Esta disciplina matemática modela estocásticamente los procesos de llegada de clientes y la capacidad de despacho del servicio para evaluar patrones de congestión y tiempos de permanencia en el sistema.
En las arquitecturas móviles contemporáneas, estos modelos matemáticos se implementan mediante plataformas de software que desvinculan la espera física de la posición geográfica del usuario. El sistema asigna un identificador secuencial único a cada solicitud entrante, calculando estimaciones del tiempo de espera y registrando el avance del turno para optimizar la distribución temporal de los usuarios.
Diversas soluciones comerciales aplican estos conceptos en sectores institucionales y comerciales (Qminder, 2024; Waitwhile, 2024). Sin embargo, la adaptación a la dinámica operativa de establecimientos universitarios de alimentos requiere arquitecturas livianas orientadas a la alta frecuencia de transacciones en periodos breves.

## 2.2 Desarrollo de Aplicaciones Móviles Multiplataforma

El desarrollo multiplataforma permite compartir lógica de negocio e interfaces de usuario entre sistemas operativos disímiles (iOS y Android), reduciendo la duplicidad de código y simplificando el mantenimiento continuo del software (Biørn-Hansen et al., 2020).
Entre los enfoques existentes destacan los esquemas basados en interpretes JavaScript, motores de renderizado propio y compilación cruzada directa. Esta última alternativa posibilita la ejecución de lógica compartida a nivel de código de máquina sin depender de puentes de abstracción en tiempo de ejecución (JetBrains, 2024).

## 2.3 Arquitectura de Software Distribuida

Una arquitectura distribuida desacopla las responsabilidades del sistema en nodos independientes interconectados por la red. La disponibilidad e integridad del servicio dependen de la correcta gestión de la latencia, la consistencia eventual y la resiliencia ante fallos de red (Coulouris et al., 2020).

### 2.3.1 El Modelo Cliente-Servidor en Entornos Móviles

En el modelo cliente-servidor para dispositivos móviles, las aplicaciones actúan como clientes que gestionan la interacción gráfica y delegan la persistencia, validación de reglas de negocio y procesamiento intensivo al servidor. No obstante, el cliente móvil asume tareas complejas como la gestión de caché, sincronización asíncrona y procesamiento local de datos para mitigar la intermitencia en la conectividad (Coulouris et al., 2020).

### 2.3.2 Arquitectura Backend as a Service (BaaS)

El paradigma Backend as a Service (BaaS) abstrae la gestión de la infraestructura subyacente proporcionando servicios administrados de autenticación, almacenamiento en la nube, bases de datos en tiempo real y mensajería a través de APIs y SDKs especializados (Google Cloud, 2024).
Este modelo agiliza el ciclo de desarrollo al externalizar la administración de servidores, aunque introduce dependencias de la plataforma proveedora y exige adaptar la arquitectura cliente a los patrones de consistencia y límites operacionales del servicio contratado (Google Cloud, 2024).

## 2.4 Sistemas de Gestión de Bases de Datos NoSQL

Las bases de datos NoSQL ofrecen esquemas flexibles y alta escalabilidad horizontal para el manejo de datos no estructurados o semiestructurados. A diferencia de los sistemas relacionales tradicionales optimizados para transacciones ACID complejas, los almacenes NoSQL priorizan el rendimiento de lectura y escritura bajo esquemas de consistencia eventual (Sadagopan, 2021).
En los modelos orientados a documentos, la información se organiza en colecciones de registros independientes (generalmente estructurados en JSON o BSON). Esta desnormalización reduce las operaciones de combinación (joins), incrementando la velocidad de acceso a costa de introducir cierta redundancia en los datos (Sadagopan, 2021).
Los mecanismos de sincronización en tiempo real propios de estos sistemas permiten notificar cambios en los documentos a los clientes suscritos mediante conexiones WebSocket o protocolos equivalentes, facilitando la actualización de la interfaz ante eventos concurrentes.
Las decisiones de diseño en bases NoSQL deben evaluar compromisos entre consistencia, disponibilidad y tolerancia al particionamiento, asegurando que la estructura de colecciones responda eficientemente a los patrones de consulta de la aplicación.

### 2.5 Control de Versiones en el Desarrollo Colaborativo

El control de versiones distribuido permite registrar las modificaciones del código fuente, facilitando la integración continua y el trabajo simultáneo mediante ramificaciones de desarrollo (Pressman & Maxim, 2020).

## 2.6 Modelo FIFO y Teoría para la Estimación de Espera

La gestión de los pedidos en AlToque se sustenta analíticamente en una disciplina de servicio de la Teoría de Colas.
Modelo FIFO (First In, First Out)
En la literatura académica, también conocido como FCFS (First Come, First Served), este algoritmo de planificación establece que las entidades son atendidas en el orden cronológico estricto de su llegada al sistema (Kendall, 1953; Gross et al., 2021). Este modelo previene el problema de inanición (starvation) temporal de solicitudes y proporciona uno de los métodos más equitativos para entornos sin sistemas de prioridad diferenciada, como lo es la comunidad universitaria.
Estimación del Tiempo de Espera
El pronóstico de los tiempos de preparación no es estático. Matemáticamente, el comportamiento de las colas a largo plazo puede modelarse mediante la Ley de Little, definida por la ecuación:
L=A X W
Donde L representa el número promedio de pedidos en el sistema, A es la tasa media de llegada de nuevos pedidos, y W es el tiempo promedio que un pedido pasa en el sistema (espera más servicio). En la aplicación práctica de este sistema, el algoritmo de estimación extrapola el tiempo de espera del usuario calculando el Tiempo Medio de Servicio basado en una media móvil de los últimos n pedidos despachados, multiplicándolo por la cantidad de turnos lógicos que le preceden en la cola virtual.

## 2.7 Proyectos Similares y Estado del Arte

La gestión de turnos y colas virtuales ha sido abordada desde diversas perspectivas en el ámbito comercial y académico. La revisión de las soluciones vigentes permite identificar patrones de diseño, ventajas operativas y brechas tecnológicas que fundamentan la propuesta de este proyecto.

### 2.7.1 Plataformas Comerciales de Gestión de Filas

Qminder: Plataforma SaaS enfocada en la atención en mostrador para retail y servicios. Ofrece registro de usuarios y métricas de atención, aunque no integra catálogo ni procesamiento de pedidos de alimentos (Qminder, 2024).
Waitwhile: Sistema de listas de espera virtuales con notificaciones SMS y análisis predictivo. Su enfoque generalista requiere configuraciones complejas para integrarse a flujos de comida rápida (Waitwhile, 2024).

### 2.7.2 Soluciones en el Contexto Latinoamericano

Virtual-T: Herramienta basada en mensajería instantánea para la asignación de turnos. Si bien reduce la barrera de entrada al usar canales existentes, carece de interfaces dedicadas para el seguimiento del menú (Virtual-T, 2024).
FilasControl: Aplicación móvil orientada al control de acceso y citas en centros de atención, con funcionalidades limitadas respecto a la personalización de pedidos (FilasControl, 2024).

### 2.7.3 Sistemas Empresariales Hardware/Software

Wavetec y Qmatic: Soluciones integrales de hardware (quioscos, dispensadores) y software para instituciones financieras y gubernamentales. Presentan costos elevados de implementación no viables para pequeñas concesiones universitarias (Qmatic, 2024; Wavetec, 2024).

### 2.7.4 Antecedentes Académicos

Diversas investigaciones universitarias han explorado la digitalización de turnos:
Díaz (2020): Diseñó un sistema móvil para la atención en centros de servicio, validando la reducción de la percepción del tiempo de espera.
Universidad Politécnica Salesiana (2021): Desarrolló una plataforma web para la reserva de citas académicas, destacando la importancia de las notificaciones push.
Reyes (2023): Implementó una solución móvil con React Native y backend en Laravel para la atención institucional, demostrando la factibilidad de frameworks multiplataforma.

### 2.7.5 Plataformas de Entrega de Alimentos (Delivery)

Aplicaciones de Marketplace (Uber Eats, Didi Food, Rappi): Establecen los estándares de experiencia de usuario para el filtrado de productos, carrito de compra y seguimiento del estado del pedido (Deliverect, 2023).
Aporte al proyecto: AlToque retiene el patrón de diseño de menú y carrito de las aplicaciones de delivery, adaptándolo al consumo presencial donde la variable crítica es la fila de espera física y no el despacho domiciliario.

# Capítulo 3. Metodología

## 3.1 Tipo de investigación

El desarrollo del proyecto AlToque se clasifica como una investigación de tipo aplicada y tecnológica. Es aplicada porque su objetivo principal es resolver un problema práctico y bien definido: la ineficiencia y los largos tiempos de espera en las cafeterías de la Universidad de Colima. Además, tiene un enfoque tecnológico porque el resultado final del proyecto es la creación de un producto de software (la aplicación móvil multiplataforma) diseñado para optimizar este proceso operativo.
Para validar los resultados, el proyecto utiliza un enfoque mixto. En el aspecto cuantitativo, se enfoca en medir numéricamente la reducción de los tiempos de espera en las filas. Por su parte, en el aspecto cualitativo, busca evaluar la satisfacción y la experiencia de uso, tanto de la comunidad universitaria que compra los alimentos como del personal que atiende en los establecimientos.

## 3.2 Metodología de Desarrollo Híbrida (Scrum y Prototipado Iterativo)

Para la ejecución y construcción del proyecto AlToque, el equipo de desarrollo adoptó una metodología híbrida que combina los principios del marco de trabajo ágil Scrum con técnicas de Prototipado Iterativo. Esta decisión se basó en la naturaleza interactiva de la aplicación, la cual exige tanto la disciplina para cumplir con las fechas de entrega del proyecto, como la flexibilidad para ajustar rápidamente el diseño de la interfaz conforme se realizan pruebas de uso.
En esta combinación metodológica:
Scrum proporciona la estructura de trabajo para planificar, asignar responsabilidades y monitorear el progreso técnico del equipo.
El Prototipado Iterativo permite diseñar y validar visualmente las pantallas y funciones de la aplicación antes de invertir tiempo en programarlas definitivamente.
Para mantener un ritmo de desarrollo ordenado, el trabajo se estructuró en ciclos cortos y continuos llamados sprints, cada uno con una duración de dos semanas. Dentro de este flujo de trabajo, se llevaron a cabo las siguientes ceremonias clave del marco Scrum:
Planificación del Sprint (Sprint Planning): Al inicio de cada ciclo, el equipo selecciona las tareas que se desarrollarán durante las próximas dos semanas. Estas tareas se extraen de un registro principal (Product Backlog), el cual se nutre de los requerimientos identificados y priorizados previamente en el proyecto.
Reuniones Diarias (Daily Standups): Breves reuniones de seguimiento para sincronizar el trabajo diario, reportar avances e identificar rápidamente si algún miembro del equipo enfrenta obstáculos técnicos.
Revisión del Sprint (Sprint Review): Al finalizar las dos semanas, el equipo presenta un incremento funcional de la aplicación (por ejemplo, el módulo de inicio de sesión terminado) para evaluar el progreso real del proyecto.
Retrospectiva (Sprint Retrospective): Sesión de análisis interno donde el equipo evalúa qué procesos funcionaron bien durante el ciclo y qué áreas de trabajo en equipo se pueden mejorar para el siguiente sprint.
Este enfoque híbrido asegura que el proyecto mantenga una velocidad de desarrollo constante y controlada, permitiendo a la vez incorporar cambios de manera ágil ante cualquier imprevisto técnico o modificación en los requerimientos iniciales del sistema.

## 3.3 Técnicas de Recolección de Requerimientos

Observación directa: Se analizó in situ el comportamiento del flujo de estudiantes y el proceso de atención durante los horarios de mayor demanda (recreos y recesos), documentando empíricamente la formación de aglomeraciones, los tiempos de espera promedio y los cuellos de botella operativos.
Entrevistas informales no estructuradas: Se sostuvieron diálogos exploratorios con la comunidad estudiantil y los empleados de los establecimientos de alimentos. Esto permitió identificar de primera mano sus necesidades, principales frustraciones respecto al modelo físico tradicional y sus expectativas sobre una solución digital.
Análisis de sistemas análogos (Benchmarking): Se examinó el estado del arte y el funcionamiento de plataformas comerciales de delivery y gestión de filas para extraer las mejores prácticas de experiencia de usuario (UX) e interfaces (UI), adaptando sus lógicas de negocio al ecosistema universitario.

## 3.4 Análisis, Diseño y Pruebas del Sistema

Este apartado describe los lineamientos metodológicos bajo los cuales se ejecutan las etapas fundamentales de la ingeniería de software, integradas armónicamente dentro del ciclo de vida iterativo del proyecto:
Análisis del Sistema: Metodológicamente, el análisis no se concibe como una fase estática y única, sino como un proceso continuo que se activa al inicio de cada ciclo de desarrollo. A través de las reuniones de planificación, las necesidades operativas de los establecimientos y los usuarios se descomponen de forma sistemática en historias de usuario prioritarias dentro del Product Backlog. Esta técnica asegura un refinamiento constante de las reglas de negocio y de las restricciones técnicas antes de proceder a la construcción del código.
Diseño del Sistema: El proceso de diseño adopta un enfoque evolutivo basado en el refinamiento progresivo. La transición desde los requerimientos abstractos hacia los componentes de software se realiza mediante el modelado de diagramas estructurales y de comportamiento de la arquitectura de la aplicación, complementado con el diseño lógico del esquema de datos no relacional. Paralelamente, se emplea un proceso de prototipado interactivo para modelar los flujos de las interfaces de usuario, permitiendo validar la navegación y la experiencia antes de escribir código definitivo.
Pruebas del Sistema: La estrategia de validación se rige por un principio de verificación incremental continua. Al finalizar cada ciclo de desarrollo, los incrementos lógicos del software se someten a un protocolo de pruebas funcionales y de integración automatizadas para garantizar la consistencia en la sincronización de datos y el correcto comportamiento de los flujos críticos. Posteriormente, se ejecutan ciclos específicos de pruebas de usabilidad y aceptación en entornos reales de operación con los grupos de interés para evaluar la eficiencia de los turnos virtuales y la experiencia de usuario final antes del despliegue en producción.

## 3.5 Tecnologías y Arquitectura Específica del Proyecto

### 3.5.1 Kotlin Multiplatform

Kotlin Multiplatform compila la lógica compartida directamente a bytecode o binarios nativos según la plataforma de destino, permitiendo ejecutar código común para la gestión de estado y comunicaciones en iOS y Android (JetBrains, 2024).

### 3.5.2 Servicios de Infraestructura en la Nube (Firebase / Google Cloud)

Para la implementación del backend se emplean los siguientes servicios administrados de Google Cloud (Google Cloud, 2024):
Firebase Authentication: Gestiona el ciclo de vida de las sesiones y la autenticación federada (OAuth 2.0).
Firebase Cloud Messaging (FCM): Proporciona la infraestructura de notificaciones push asíncronas hacia APNs (iOS) y FCM (Android).
Cloud Firestore: Almacén de documentos NoSQL con soporte de sincronización bidireccional en tiempo real y persistencia en caché local.

### 3.5.3 Estructura de la Base de Datos NoSQL

La estructura de Cloud Firestore comprende las colecciones principales de usuarios, establecimientos, productos, turnos y pedidos, además de la colección empleadoMetricasDiarias para el registro del desempeño operativo del personal.
Los pedidos deberán conservar su relación con el cliente, el establecimiento y el turno asignado, así como los productos adquiridos, el importe, el estado operativo y la referencia y el estado del pago verificado por el backend. Esta asociación permitirá identificar la transacción que originó cada pedido y evitar la generación de pedidos o turnos duplicados.
La colección empleadoMetricasDiarias almacenará la identificación del empleado y del establecimiento, la fecha operativa y la información de las sesiones de trabajo, incluyendo la estación desempeñada, los horarios de inicio y cierre y las cantidades de pedidos preparados o entregados. Los resultados deberán conservar su asociación con cada sesión para permitir la acumulación diaria sin duplicidades. Los pedidos marcados como “No Reclamado” no se contabilizarán como entregados.
La primera versión no contemplará el almacenamiento de tarjetas para una Wallet interna. Los datos de pago conservados en Cloud Firestore se limitarán a la información necesaria para identificar y verificar la transacción y vincularla con el pedido.
Ilustración 1 diseño de la base de datos NoSQL de la aplicación AlToque

### 3.5.4 Control de Versiones y Flujo de Trabajo (Git + GitHub)

La gestión del código fuente utiliza Git bajo el modelo GitFlow en GitHub, implementando ramas de características (feature branches) y validación previa mediante Pull Requests y GitHub Actions (Pressman & Maxim, 2020).

### 3.5.5 Integración de Pagos mediante Mercado Pago Checkout Pro

AlToque incorporará Mercado Pago Checkout Pro para que el cliente complete el pago en el entorno de Mercado Pago. Desde la aplicación desarrollada con Kotlin Multiplatform, el cliente podrá iniciar el proceso de pago y regresar posteriormente para consultar el resultado y dar seguimiento a su pedido.
El backend será responsable de gestionar la comunicación con Mercado Pago y verificar el estado de la transacción, su importe y su asociación con el pedido correspondiente. El regreso del cliente a la aplicación no se considerará evidencia suficiente de un pago aprobado. Únicamente después de verificar su aprobación se confirmará el pedido, se asignará el turno y se habilitará el código QR.
Los pagos pendientes permanecerán identificados como tales hasta obtener un resultado definitivo. El procesamiento deberá impedir que las notificaciones repetidas o los reintentos de una misma transacción generen pedidos o turnos duplicados. La información necesaria para el seguimiento del pago se conservará en Cloud Firestore.
Esta integración no incluirá formularios propios para registrar tarjetas ni una Wallet interna. La incorporación de Mercado Pago Checkout API y la administración de tarjetas dentro de AlToque se contemplan como mejoras futuras.

# Capítulo 4. Análisis del Sistema

## 4.1 Identificación de Actores del Sistema

Dentro de la arquitectura y el ecosistema operativo de AlToque, se han identificado los siguientes actores principales, definidos por el nivel de permisos y las interacciones que mantienen con la plataforma:
Usuario Cliente (Estudiante/Personal): Actor final que utiliza la aplicación móvil para explorar el catálogo de productos, solicitar un turno virtual en el establecimiento de su elección, monitorear en tiempo real su posición en la fila, y presentar su código QR de validación al momento de recoger su pedido.
Empleado de Cafetería (Operativo): Actor encargado de gestionar el flujo continuo de pedidos. Sus interacciones principales incluyen visualizar la cola de atención, actualizar el estado de las órdenes (ej. "en preparación", "listo para entrega") y escanear el código QR del Usuario Cliente para concretar y liberar el turno.
Gerente de Cafetería: Actor responsable de la administración local del establecimiento. Cuenta con los privilegios necesarios para gestionar el menú a través de operaciones CRUD (crear, leer, actualizar y eliminar platillos, bebidas o botanas), modificar precios y controlar la disponibilidad del inventario.
Administrador General (CEO): Actor responsable de la administración global de AlToque. Sus funciones incluyen registrar, consultar, modificar y dar de baja establecimientos, así como gestionar la asignación del personal a estos. Puede vincular usuarios previamente registrados con un establecimiento, asignarles el rol de Gerente de Cafetería o Empleado Operativo, modificar dichas asignaciones y revocar los permisos correspondientes. La revocación de una asignación no implica eliminar la cuenta general del usuario. Estas facultades administrativas no permiten cancelar pedidos pagados

## 4.2 Requerimientos Funcionales

Los requisitos funcionales delinean las funcionalidades específicas que el sistema AlToque debe proporcionar a sus usuarios. Estos requisitos han sido identificados a través de entrevistas con los stakeholders, el análisis de aplicaciones análogas y la observación directa del proceso de atención en los establecimientos universitarios. A continuación, se presentan los principales requisitos funcionales, clasificados por módulo del sistema:
RF-001: El sistema deberá permitir el registro e inicio de sesión mediante una cuenta de Google, Apple ID o un número de teléfono con verificación SMS, utilizando Firebase Authentication.
RF-002: El sistema deberá solicitar la verificación de identidad del Usuario Cliente mediante un documento oficial, como INE o equivalente, antes de permitirle realizar pedidos.
RF-003: El sistema deberá asignar un turno al pedido únicamente después de que el backend confirme la aprobación del pago. El cliente podrá consultar su posición en la fila, el tiempo de espera estimado y el estado del pedido. La asignación y la atención de los turnos deberán respetar el criterio FIFO establecido para AlToque.
RF-004: El sistema deberá emitir notificaciones push para informar al cliente cuando su turno se aproxime, cuando comience la preparación y cuando el pedido se encuentre listo para recoger. El aviso de proximidad se emitirá conforme al umbral configurado, establecido inicialmente en tres turnos anteriores al del cliente.
RF-005: El sistema deberá impedir, sin excepciones y para todos los roles, la cancelación de pedidos una vez confirmado su pago. Cuando un pedido permanezca en estado “Listo” y supere el plazo establecido para su recolección, el empleado de la estación de Entrega podrá marcarlo como “No Reclamado”, previa confirmación. Esta acción deberá retirarlo de la vista de pendientes y finalizar su ciclo operativo, conservando su historial, sin modificar el pago ni generar reembolsos o procesos adicionales. Las aclaraciones por pedidos no recogidos deberán gestionarse físicamente y en persona con el gerente del establecimiento, fuera del alcance del software.
RF-006: El sistema deberá generar un número de pedido y un código QR único para cada pedido con pago confirmado. Antes de registrar la entrega, deberá validar que el QR corresponda al establecimiento y a un pedido en estado “Listo”. Un pedido previamente entregado o marcado como “No Reclamado” no podrá registrarse nuevamente como entregado.
RF-007: El sistema deberá permitir al cliente consultar el menú de los establecimientos, filtrar productos por categoría y gestionar su carrito mediante la incorporación, eliminación y modificación de cantidades de productos. Antes de iniciar el pago, deberá validar la disponibilidad y el importe vigente de los productos seleccionados.
RF-008: El sistema deberá controlar el acceso a las funciones y los datos mediante los roles de Administrador General (CEO), Gerente de Cafetería y Empleado Operativo. Las autorizaciones deberán verificarse en el backend y limitarse al establecimiento y las responsabilidades correspondientes, sin depender únicamente de los controles visibles en la interfaz.
RF-009: El sistema deberá permitir al Gerente de Cafetería crear, consultar, editar y dar de baja productos del menú de su establecimiento, incluyendo fotografía, nombre, descripción, precio y categoría. Asimismo, podrá controlar su disponibilidad y restablecer la venta de productos marcados como agotados temporalmente. La baja de un producto deberá conservar la información histórica de los pedidos que lo incluyan.
RF-010: El sistema deberá proporcionar funciones diferenciadas para las estaciones del Empleado Operativo. Cocina podrá consultar pedidos recibidos, cambiarlos a “En Preparación” y posteriormente a “Listo”. Entrega podrá consultar pedidos listos, escanear y validar códigos QR, confirmar el estado “Entregado” y marcar pedidos como “No Reclamado” conforme al RF-005. El Cajero podrá consultar y verificar transacciones e incidencias técnicas, sin aprobar manualmente cobros ni modificar importes o estados de pago.
RF-011: El sistema deberá permitir iniciar el pago mediante Mercado Pago Checkout Pro, redirigiendo al cliente al entorno de Mercado Pago. El backend deberá verificar el resultado de la transacción antes de confirmar el pedido. Si el pago permanece pendiente, la aplicación mostrará esta condición sin iniciar otro cobro para la misma operación. Si el pago es rechazado o el intento finaliza sin cobro confirmado, permitirá reintentarlo. El regreso del cliente a AlToque no deberá considerarse prueba de aprobación del pago.
RF-012: El sistema deberá asociar cada transacción aprobada con un único pedido y su turno correspondiente. Las solicitudes repetidas, las actualizaciones de estado del pago y los reintentos de procesamiento no deberán generar pedidos ni turnos duplicados.
RF-013: El sistema deberá permitir al Empleado Operativo iniciar una sesión de trabajo y seleccionar una estación autorizada entre Cocina, Entrega y Cajero. Registrará el empleado, el establecimiento, la estación, la fecha y hora de inicio y un identificador de sesión. La selección de estación no deberá conceder privilegios de Gerente de Cafetería ni de Administrador General.
RF-014: El sistema deberá proporcionar al empleado de Cocina un Dashboard Operativo que muestre la cantidad de pedidos en estado “Recibido”, “En Preparación” y “Listo”, así como las cantidades de productos pendientes de elaborar. Esta información deberá actualizarse en tiempo real para apoyar la organización de la producción en tandas, sin alterar la prioridad de atención FIFO. Los pedidos entregados o no reclamados deberán excluirse de la carga activa.
RF-015: El sistema deberá permitir al Empleado Operativo marcar productos de su establecimiento como “Agotado temporalmente”. Cuando se marque un insumo, la restricción deberá aplicarse a los platillos afectados mediante una relación previamente definida o su selección explícita. La actualización deberá ocultar los productos afectados del menú disponible para compra y bloquear nuevos pagos por ellos, incluso si permanecen en un carrito anterior. Esta función no permitirá modificar precios ni otros datos generales del catálogo y no cancelará pedidos previamente pagados.
RF-016: El sistema deberá permitir al empleado finalizar su turno y consultar un resumen de su desempeño durante la sesión. Registrará en la colección empleadoMetricasDiarias de Cloud Firestore la identificación del empleado y del establecimiento, la estación, la sesión, la fecha, los horarios de inicio y cierre y las cantidades de pedidos preparados o entregados que correspondan. Los pedidos no reclamados no se contabilizarán como entregados y los reintentos de cierre no duplicarán las métricas. El cierre de turno no modificará el estado de los pedidos pendientes del establecimiento.
RF-017: El sistema deberá permitir al Administrador General (CEO) consultar al personal y asignar usuarios previamente registrados a un establecimiento con el rol de Gerente de Cafetería o Empleado Operativo. También podrá modificar el rol o establecimiento de una asignación y revocar sus permisos. Estas acciones no deberán eliminar la cuenta general del usuario ni los registros históricos de sus actividades.
RF-018: El sistema deberá permitir al Administrador General (CEO) registrar, consultar, modificar y dar de baja establecimientos, incluyendo su fotografía, nombre, dirección, descripción, categoría y horario. La baja deberá impedir nuevas compras en el establecimiento sin eliminar el historial de pedidos, pagos y métricas, ni cancelar pedidos previamente pagados.
RF-019: El sistema deberá permitir al Usuario Cliente consultar su historial de pedidos finalizados y, después de una entrega, registrar opcionalmente una calificación y una reseña del establecimiento.
RF-020: El sistema deberá validar las transiciones de estado de los pedidos y registrar el empleado responsable, su sesión y la fecha y hora de cada operación. El ciclo operativo comprenderá los estados “Recibido”, “En Preparación”, “Listo” y “Entregado”, con la alternativa de pasar de “Listo” a “No Reclamado” conforme al RF-005. Las acciones simultáneas o repetidas no deberán producir transiciones incompatibles ni duplicar los conteos operativos.

## 4.3 Requerimientos No Funcionales

Los requisitos no funcionales (RNF) definen los criterios de calidad que el sistema está obligado a satisfacer, incluyendo atributos fundamentales como el rendimiento, la seguridad, la usabilidad y la escalabilidad. La observancia rigurosa de estos requisitos resulta indispensable para garantizar que la solución no solo opere con eficiencia, sino que también proporcione una experiencia de usuario altamente satisfactoria y preserve la capacidad de adaptación ante el crecimiento futuro de la plataforma.
RNF-001 (Rendimiento): El sistema debe garantizar un tiempo de respuesta a las solicitudes de los usuarios inferior a 2 segundos bajo condiciones operacionales estándar. Adicionalmente, las actualizaciones de la posición en la cola deberán reflejarse en tiempo real, manteniendo una latencia máxima admisible de 500 milisegundos.
RNF-002 (Escalabilidad): Se requiere que el sistema soporte simultáneamente un mínimo de 1,000 usuarios concurrentes y 100 establecimientos operativos sin incurrir en una degradación sustancial del rendimiento. Asimismo, deberá disponer de la capacidad de escalamiento horizontal apropiada en función de la demanda.
RNF-003 (Seguridad): Es imperativo que el sistema implemente mecanismos de autenticación robustos, específicamente a través de OAuth 2.0 y Firebase Auth, junto con el cifrado de datos tanto en tránsito (mediante el protocolo HTTPS) como en reposo. Además, se deberá garantizar la protección efectiva contra las vulnerabilidades de seguridad más frecuentes, conforme a la clasificación OWASP Top 10 (Open Worldwide Application Security Project) .
RNF-004 (Usabilidad): La interfaz de usuario ha de ser diseñada para ser altamente intuitiva, facilitando que un usuario sin experiencia previa logre completar su primer pedido en un lapso no superior a 3 minutos y sin requerir asistencia. El sistema deberá incluir soporte para los idiomas español e inglés.
RNF-005 (Disponibilidad): El sistema deberá mantener un índice de disponibilidad mensual del 99.5%, mediante la implementación de mecanismos de recuperación ante fallos y la provisión de respaldo de datos gestionados en Firebase.
RNF-006 (Compatibilidad): La aplicación debe ser compatible con dispositivos que operen bajo las versiones iOS 14.0 o subsiguientes y Android 8.0 (API 26) o superiores, asegurando su correcta adaptación a diversas dimensiones y resoluciones de pantalla.

## 4.4 Diagramas de Casos de Uso

### 4.4.1. Diagrama de Casos de Uso: Usuario Cliente (App Móvil)

Descripción: Este diagrama de casos de uso representa las funciones disponibles para el Usuario Cliente en la aplicación móvil AlToque.
Detalles: Ilustra la relación entre el “Usuario Cliente” y las acciones que puede realizar en la plataforma: registrarse e iniciar sesión, verificar su identidad, consultar el menú, gestionar el carrito y pagar mediante Mercado Pago Checkout Pro. Una vez confirmado el pago, el sistema asigna el turno y genera el código QR correspondiente. El cliente puede consultar su posición en la fila y el estado del pedido, mostrar el QR para recogerlo, revisar su historial y calificar o reseñar el establecimiento. La primera versión no incluye una Wallet interna ni el registro de tarjetas dentro de AlToque. Los pedidos pagados no pueden cancelarse.
Ilustración 2 Diagrama caso de uso para el rol de cliente

### 4.4.2. Diagrama de Casos de Uso del Gerente de Cafetería

Descripción: Este diagrama de casos de uso representa las funciones del Gerente de Cafetería dentro del módulo de administración local de AlToque, relacionadas con la gestión del menú, los precios, el inventario y la disponibilidad de los productos de su establecimiento, así como la visualización de la cola de turnos.
Detalles: Ilustra la relación entre el “Gerente de Cafetería” y las acciones administrativas disponibles en la plataforma. Sus funciones incluyen gestionar el menú mediante la creación, consulta, edición y eliminación de productos, modificar precios, controlar el inventario y la disponibilidad de los platillos, restablecer la disponibilidad de productos marcados como agotados temporalmente y visualizar la cola de turnos. Este perfil no dispone de funciones para cancelar pedidos pagados ni generar reembolsos. Las aclaraciones que requieran atención presencial del gerente quedan fuera del alcance del software.
Ilustración 3 Diagrama Caso de Uso Rol Gerente

### 4.4.3 Casos de Uso: Administrador General (CEO)

Descripción: Este diagrama de casos de uso representa las funciones del Administrador General (CEO) dentro del módulo de administración global de AlToque.
Detalles: Ilustra la relación entre el “Administrador General (CEO)” y las funciones de gestión de establecimientos y personal. Este actor puede registrar, consultar, modificar y dar de baja establecimientos. Asimismo, puede consultar al personal, asignar usuarios previamente registrados a un establecimiento con el rol de Gerente de Cafetería o Empleado Operativo, modificar su rol o establecimiento y revocar sus permisos. La revocación de una asignación no elimina la cuenta general del usuario ni los registros históricos de sus actividades. Estas facultades administrativas no permiten cancelar pedidos pagados
Ilustración 4 Descripción de Administrador General CEO

### 4.4.4.Diagrama de Casos de Uso del Empleado Operativo

Funciones comunes del Empleado Operativo
Descripción: Este diagrama de casos de uso representa las funciones compartidas por los empleados de las estaciones de Cocina, Entrega y Cajero.
Detalles: El Empleado Operativo puede iniciar su turno seleccionando la estación de trabajo correspondiente y marcar productos o insumos como “Agotado temporalmente”, lo que oculta los platillos afectados en la aplicación del cliente sin conceder permisos de administración general del catálogo. Al finalizar el turno, el sistema presenta un resumen del desempeño de la sesión y registra las métricas correspondientes en la colección empleadoMetricasDiarias de Cloud Firestore.
Ilustración 5 Empleado Operativo Funciones Comunes
Estación Cocina
Descripción: Este diagrama de casos de uso representa las funciones del Empleado Operativo que selecciona la estación de Cocina al iniciar su turno.
Detalles: El empleado consulta los pedidos en estado “Recibido”, inicia su elaboración mediante el cambio a “En Preparación” y los marca como “Listo” cuando concluye su preparación. Asimismo, consulta el Dashboard Operativo para visualizar el volumen total de pedidos en espera y organizar la producción en tandas, respetando el criterio FIFO. Los pedidos que ya se encuentran en preparación permanecen disponibles para su seguimiento hasta quedar listos para entrega.
Ilustración 6 Empleado Operativo Estacion Cocina
Estación Entrega
Descripción: Este diagrama de casos de uso representa las funciones del Empleado Operativo responsable de entregar los pedidos a los clientes.
Detalles: El empleado consulta los pedidos en estado “Listo” y utiliza la cámara del dispositivo para escanear y validar el código QR del cliente antes de confirmar el estado “Entregado”. Cuando un pedido supera el tiempo establecido para su recolección, puede marcarlo como “No Reclamado”, exclusivamente para retirarlo de la pantalla de pendientes y finalizar su ciclo operativo, conservando su registro en Cloud Firestore. Esta acción no cancela el pedido pagado ni genera reembolsos o procesos adicionales.
Ilustración 7 Empleado Operativo Estacion Entrega
Estación Cajero
Descripción: Este diagrama de casos de uso representa las funciones del Empleado Operativo que desempeña sus actividades en la estación de Cajero.
Detalles: El empleado verifica las transacciones y consulta incidencias técnicas relacionadas con los pedidos mediante la información registrada en el sistema. Esta estación no permite cancelar pedidos pagados ni generar reembolsos. Cualquier aclaración que no pueda resolverse mediante las herramientas de consulta deberá gestionarse físicamente y en persona con el gerente del establecimiento, fuera del alcance del software.
Ilustración 8 Empleado Operativo Estacion Cajero

## 4.5 Flujos de Trabajo de AlToque

Este apartado presenta los flujos de trabajo de AlToque, describiendo la secuencia de actividades, decisiones y rutas alternativas que intervienen en la operación de la aplicación. Estos diagramas complementan los casos de uso al representar cómo se desarrollan las interacciones entre los usuarios y el sistema.

### 4.5.1 Flujo de Compra y Seguimiento del Pedido del Cliente

Descripción: Este diagrama de flujo representa el recorrido del Usuario Cliente desde la selección de productos hasta la finalización de su pedido, utilizando Mercado Pago Checkout Pro como mecanismo de pago.
Detalles: El proceso comienza en la pantalla de inicio, donde el cliente selecciona los productos del establecimiento y revisa las cantidades y el importe de su carrito. Antes de iniciar el pago, el sistema verifica la disponibilidad de los productos. Posteriormente, el cliente accede al entorno de Mercado Pago mediante Checkout Pro, donde selecciona un medio de pago y realiza la operación. AlToque verifica el resultado de la transacción mediante el backend; el regreso del cliente a la aplicación no constituye, por sí mismo, una confirmación del cobro.
Si el pago es rechazado o el intento concluye sin cobro confirmado, el sistema informa la situación y permite volver al carrito para reintentarlo. Si la transacción permanece pendiente, la aplicación muestra esta condición hasta disponer de un resultado definitivo, sin solicitar un nuevo pago para la misma operación.
Una vez aprobado el pago, el sistema confirma el pedido, asigna el turno y habilita el código QR y el seguimiento en el apartado de Órdenes. El cliente consulta los estados “Recibido”, “En Preparación” y “Listo”. Cuando el pedido está disponible, recibe una notificación y se presenta en el establecimiento para mostrar su código QR. El empleado valida el código, entrega los productos y confirma el estado “Entregado”. Si el cliente no acude y se supera el plazo establecido, el empleado de Entrega puede marcar el pedido como “No Reclamado”, sin generar reembolsos ni procesos adicionales. Ambos estados concluyen el ciclo operativo y conservan el pedido en el historial. Después de una entrega, el cliente puede calificar opcionalmente su experiencia.
El flujo no contempla el registro de tarjetas ni una Wallet interna, debido a que estas funcionalidades quedan fuera del alcance de la primera versión. Una vez confirmado el pago, no se permite cancelar el pedido.
Ilustración 9 Fluo Cliente

### 4.5.2 Flujo de Gestión de Empleados

Descripción: Este diagrama de flujo representa el proceso mediante el cual el Administrador General (CEO) consulta, asigna, modifica y revoca los permisos del personal vinculado a los establecimientos de AlToque.
Detalles: El proceso comienza cuando el CEO accede al módulo “Empleados”. El sistema presenta una lista del personal asignado, un buscador y la opción para añadir una nueva asignación. Mediante el buscador, el CEO puede localizar personas por nombre o correo electrónico y filtrar los resultados en pantalla.
Para realizar una asignación, el CEO ingresa el correo electrónico del usuario, selecciona el establecimiento y establece el rol de Gerente de Cafetería o Empleado Operativo. El sistema verifica que el correo corresponda a una cuenta previamente registrada en AlToque. Si la cuenta existe, guarda la asignación y los permisos correspondientes en Cloud Firestore; en caso contrario, informa que la persona debe registrarse previamente en la aplicación.
Al seleccionar una persona de la lista, el sistema muestra sus datos y las opciones para editar o eliminar su asignación. La edición permite modificar el rol o el establecimiento asociado. La eliminación revoca los permisos de esa asignación, sin eliminar la cuenta general del usuario ni los registros históricos de sus actividades.
Ilustración 10 CRUD Empleados

### 4.5.3 Flujo de Gestión del Menú

Descripción: Este diagrama de flujo representa las operaciones de creación, consulta, actualización y eliminación de productos del menú, realizadas por el Gerente de Cafetería dentro del establecimiento que administra.
Detalles: El proceso inicia cuando el gerente accede al módulo “Menú”. El sistema presenta los productos registrados, un buscador y la opción para agregar un nuevo elemento. El buscador permite localizar productos por nombre o categoría dentro del establecimiento autorizado.
Al seleccionar la opción de agregar, el sistema muestra un formulario para registrar la fotografía, el nombre, la descripción, el precio y la categoría del producto, manteniendo su asociación con el establecimiento correspondiente. Una vez completada la información, el sistema guarda el nuevo producto en Cloud Firestore.
Cuando el gerente selecciona un producto existente, la aplicación muestra sus detalles y las opciones para editarlo o eliminarlo. Si elige editar, se presenta un formulario con los datos actuales para efectuar y guardar las modificaciones. Si selecciona eliminar, el sistema solicita confirmación antes de retirar el producto del catálogo. Si no se confirma la operación, el producto permanece sin cambios. Estas acciones corresponden a la administración del menú y son distintas del permiso limitado del Empleado Operativo para marcar productos como agotados temporalmente.
Ilustración 11 CRUD Menu
4.5.4 Flujo de Gestión de Establecimientos
Descripción: Este diagrama de flujo representa el proceso mediante el cual el Administrador General (CEO) consulta, registra, modifica y elimina establecimientos dentro de AlToque.
Detalles: El proceso comienza en el panel principal, cuando el CEO selecciona la opción “Gestionar establecimiento”. El sistema muestra una vista con los establecimientos registrados, un buscador y la opción para agregar un nuevo establecimiento. La búsqueda se realiza mediante el nombre del establecimiento y actualiza los resultados presentados en pantalla.
Para registrar un establecimiento, el CEO accede al formulario correspondiente e ingresa su fotografía, nombre, dirección, descripción, categoría y horario. El sistema guarda la información en Cloud Firestore para incorporarlo al registro de establecimientos de la plataforma.
Al seleccionar un establecimiento existente, se muestran sus datos y las opciones de edición o eliminación. La edición permite abrir un formulario con la información previamente registrada, modificar los campos necesarios y guardar los cambios. Para eliminar el establecimiento, el sistema solicita una confirmación explícita. Si el CEO confirma la operación, se ejecuta la eliminación del registro; en caso contrario, se conserva la información sin modificaciones.
Ilustración 12 CRUD Establecimientos

## 4.6 Diagramas de Secuencia

### 4.6.1 Diagrama de Secuencia de Asignación de un Turno con Pago Confirmado

Descripción: Este diagrama de secuencia representa las interacciones entre los componentes de AlToque para confirmar un pedido y asignar su turno, una vez que el backend ha verificado la aprobación del pago mediante Mercado Pago Checkout Pro.
Detalles: La secuencia parte de una transacción cuyo pago ya se encuentra confirmado. El backend valida la asociación de la transacción con el usuario y el establecimiento, y comprueba que no haya sido utilizada previamente para confirmar el mismo pedido. A continuación, registra en Cloud Firestore el pedido en estado “Recibido” y su turno asociado, y genera el código QR correspondiente.
La aplicación del cliente obtiene la información necesaria para mostrar el pedido y su posición en la fila, mientras que la interfaz del empleado recibe la actualización de la cola de atención. Las solicitudes repetidas de una misma transacción no deberán generar pedidos ni turnos duplicados. Los pagos pendientes o rechazados no habilitan la asignación de un turno. El procesamiento del pago en Mercado Pago es previo a esta secuencia y se describe en el flujo de compra del cliente.
Ilustración 13 Diagrama de Secuencia de Asignación de un Turno con Pago Confirmado

# Referencias

Biørn-Hansen, A., Rieger, C., Grønli, T.-M., Majchrzak, T. A., & Ghinea, G. (2020). An empirical investigation of performance and device resource usage in cross-platform mobile development. Empirical Software Engineering, 25(4), 2454–2498. https://doi.org/10.1007/s10664-020-09827-4
Coulouris, G., Dollimore, J., Kindberg, T., & Blair, G. (2020). Distributed systems: Concepts and design (6th ed.). Pearson Education.
Deliverect. (2023). The future of online food delivery: Trends and technology integration. Deliverect Insights.
Díaz, B. (2020). Diseño de una aplicación móvil para la gestión de turnos en un centro de atención al cliente \[Tesis de grado, Escuela Superior Politécnica del Litoral\]. Repositorio Institucional ESPOL. https://dspace.espol.edu.ec/handle/123456789/39137
FilasControl. (2024). Sistema de administración de filas virtual: Documentación de la plataforma móvil. FilasControl. https://www.filascontrol.com/app-movil
Google Cloud. (2024). Firebase documentation and technical specifications. Google Cloud Platform. https://firebase.google.com/docs
Gross, D., Shortle, J. F., Thompson, J. M., & Harris, C. M. (2018). Fundamentals of queueing theory (5th ed.). John Wiley & Sons.
JetBrains. (2024). Kotlin Multiplatform documentation and architectural overview. JetBrains. https://kotlinlang.org/docs/multiplatform.html
Pressman, R. S., & Maxim, B. R. (2020). Software engineering: A practitioner's approach (9th ed.). McGraw-Hill Education.
Qmatic. (2024). Customer journey management solutions: Enterprise overview. Qmatic. https://www.qmatic.com
Qminder. (2024). Queue management system features and API documentation. Qminder. https://www.qminder.com
Reyes, Z. (2023). Desarrollo de aplicación móvil para la gestión de turnos institucionales \[Tesis de grado, Universidad Estatal del Sur de Manabí\]. Repositorio UNESUM.
Sadagopan, S. (2021). NoSQL databases: Principles, architecture, and applications. Springer.
Universidad Politécnica Salesiana. (2021). Desarrollo de aplicación web para gestión de turnos en la escuela de posgrados. Repositorio Institucional UPS.
Virtual-T. (2024). Sistema de turnos virtual vía WhatsApp. Virtual-T. https://www.virtual-t.com
Waitwhile. (2024). Virtual queue platform overview. Waitwhile. https://www.waitwhile.com
Wavetec. (2024). Queue management solutions. Wavetec. https://www.wavetec.com