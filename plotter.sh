
export CP=lib/RXTXcomm.jar:lib/core.jar:lib/serial.jar:lib/geomerative.jar:out/production/Kritzler

java -d32 -Djava.library.path=lib -cp $CP com.tinkerlog.kritzler.Plotter
