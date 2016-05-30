#include "DevStoneCoupledHO.h"

DevStoneCoupledHO::DevStoneCoupledHO(const std::string& prefix, int width, int depth, double preparationTime, double intDelayTime, double extDelayTime)
	: Coupled(prefix),
	  iIn("in"),
	  iInAux("inAux"),
	  oOut("out"),
	  oOutAux("outAux") {
	Component::addInPort(&iIn);
	Component::addInPort(&iInAux);
	Component::addOutPort(&oOut);
	Component::addOutPort(&oOutAux);
	char buffer[40];
	snprintf(buffer, sizeof(buffer), "%d", depth-1);
	this->name = prefix;
	this->name.append(buffer);
	if (depth == 1) {
		DevStoneAtomic* atomic = new DevStoneAtomic(std::string("A1_") + name, preparationTime, intDelayTime, extDelayTime);
		Coupled::addComponent(atomic);
		Coupled::addCoupling(this, &iIn, atomic, &atomic->iIn);
		Coupled::addCoupling(atomic, &atomic->oOut, this, &oOut);
	} else {
		DevStoneCoupledHO* coupled = new DevStoneCoupledHO(prefix, width, depth - 1, preparationTime, intDelayTime, extDelayTime);
		Coupled::addComponent(coupled);
		Coupled::addCoupling(this, &iIn, coupled, &coupled->iIn);
		Coupled::addCoupling(this, &iIn, coupled, &coupled->iInAux);
		Coupled::addCoupling(coupled, &coupled->oOut, this, &oOut);
		DevStoneAtomic* atomicPrev = 0;
		for (int i = 0; i < (width - 1); ++i) {
			snprintf(buffer, sizeof(buffer), "%d", i+1);
			DevStoneAtomic* atomic = new DevStoneAtomic(std::string("A") + buffer + "_" + name, preparationTime, intDelayTime, extDelayTime);
			Coupled::addComponent(atomic);
			Coupled::addCoupling(this, &iInAux, atomic, &atomic->iIn);
			Coupled::addCoupling(atomic, &atomic->oOut, this, &oOutAux);
			if(atomicPrev!=0) {
				Coupled::addCoupling(atomicPrev, &atomicPrev->oOut, atomic, &atomic->iIn);
			}
			atomicPrev = atomic;
		}
	}
}


DevStoneCoupledHO::~DevStoneCoupledHO() {
	auto components = Coupled::getComponents();
	for(auto component : components) {
		delete component;
	}
}

