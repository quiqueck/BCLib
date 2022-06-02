package ru.bclib.interfaces;

public interface FrozableRegistry {
	void setFrozeState(boolean frozen);
	boolean getFrozeState();
}
