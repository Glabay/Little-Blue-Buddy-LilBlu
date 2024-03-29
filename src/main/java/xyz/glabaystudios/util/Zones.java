package xyz.glabaystudios.util;

import lombok.Getter;

@Getter
public enum Zones {

	ECT("European Central Time",        +1.00),
	EET("Eastern European Time",        +2.00),
	EAT("Eastern African Time",         +3.00),
	MET("Middle East Time",             +3.30),
	NET("Near East Time",               +4.00),
	PLT("Pakistan Lahore Time",         +5.00),
	IST("India Standard Time",          +5.30),
	BST("Bangladesh Standard Time",     +6.00),
	VST("Vietnam Standard Time",        +7.00),
	CTT("China Taiwan Time",            +8.00),
	JST("Japan Standard Time",          +9.00),
	ACT("Australia Central Time",       +9.30),
	AET("Australia Eastern Time",       +10.00),
	SST("Solomon Standard Time",        +11.00),
	NST("New Zealand Standard Time",    +12.00),
	MIT("Midway Islands Time",          -11.00),
	HST("Hawaii Standard Time",         -10.00),
	AKST("Alaska Standard Time",        -9.00),
	PST("Pacific Standard Time",        -8.00),
	MST("Mountain Standard Time",	       -7.00),
	CST("Central Standard Time",        -6.00),
	EST("Eastern Standard Time",        -5.00),
	AST("Atlantic Standard Time",       -4.00),
	CNT("Canada Newfoundland Time",     -3.30),
	AGT("Argentina Standard Time",      -3.00),
	BET("Brazil Eastern Time",          -3.00),
	CAT("Central African Time",         -1.00);

	private final String zoneName;
	private final Double offset;

	Zones(String name, double offset) {
		this.zoneName = name;
		this.offset = offset;
	}
}
