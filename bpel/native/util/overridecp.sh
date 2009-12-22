#!/bin/sh
# Copyright(c) 2009 Red Hat Middleware, LLC,
# and individual contributors as indicated by the @authors tag.
# See the copyright.txt in the distribution for a
# full listing of individual contributors.
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library in the file COPYING.LIB;
# if not, write to the Free Software Foundation, Inc.,
# 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
#
# @author Jean-Frederic Clere

# Copy files according a rule like Override.
# $1: source dir
# $2: product (apr, tomcat-native etc)
# $3: version
# $4: source file (file name)
# $5: dest dir
#
# example:
# overridecp $build_top/../../srclib/ ${name_prod} ${version_prod} NMAKEmakefile $src_dir
#
basedir=$1
prod=$2
versionstring=$3
file=$4
destdir=$5
verpart=`echo $versionstring | sed 's:\.: :g'`
verpart1=`echo $verpart | awk '{ print $1 }'`
verpart2=`echo $verpart | awk '{ print $2 }'`

srcfile="" 
if [ -f ${basedir}/${prod}/${file} ]; then
  srcfile=${basedir}/${prod}/${file}
fi
if [ -f ${basedir}/${prod}/${prod}-${verpart1}/${file} ]; then
  srcfile=${basedir}/${prod}/${prod}-${verpart1}/${file}
fi
if [ -f ${basedir}/${prod}/${prod}-${verpart1}.${verpart2}/${file} ]; then
  srcfile=${basedir}/${prod}/${prod}-${verpart1}.${verpart2}/${file}
fi
if [ -f ${basedir}/${prod}/${prod}-${versionstring}/${file} ]; then
  srcfile=${basedir}/${prod}/${prod}-${versionstring}/${file}
fi
if [ "${srcfile}x" != "x" ]; then
  echo "copying $srcfile $destdir"
  cp $srcfile $destdir
fi
