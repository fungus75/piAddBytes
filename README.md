# piAddBytes
PI Module to add some bytes (at begin or end, i.E. "BOM")

This module adds some bytes at the begin or the end of a message. It could be used to create the BOM https://en.wikipedia.org/wiki/Byte_order_mark

# Configuration
This module needs that parameters:

## position
where to add the bytes. Specify "top" for beginning or "end" for in the end.

## bytes
bytes to add, seperated by comma.
i.E. 239, 187, 191
that adds the UTF-8 BOM

You can also use hexadecimal values if they start with an "x":
xEF, xBB, xBF
whould be the same as 239, 187, 191

# Installation
To deploy this module to a SAP PI System you have to install NWDS in the version of your PI and follow this documentation to generate the neccessary EJB: https://blogs.sap.com/2015/01/29/create-sap-pi-adapter-modules-in-ejb-30-standard/

cu DerPilz
