JFLAGS= 
JC= javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	PaddedPrimitive.java \
	WaitFreeQueue.java \
	StopWatch.java \
	Fingerprint.java \
	RandomGenerator.java \
	PacketGenerator.java \
	AccessControl.java \
	ParallelAccessControl.java \
	ParallelPacketWorker.java \
	ParameterMix.java \
	PacketWorker.java \
	ParallelAccessControlVer3.java \
	AtomicQueue.java \
	AtomicQueueApp.java \
	FireWall.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class