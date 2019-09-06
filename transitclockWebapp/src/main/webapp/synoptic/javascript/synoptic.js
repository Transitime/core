var doStuff = function (indice,top, left ) {
	this.__showContexMenuFromMuli({ top, left },indice)
	 }



/**
 * It defines a synoptic
 * The contructor receivs as paramters:
 * container:div|window
 * onclick:callback
 * border:int 
 * routeName:string
 * showReturn:true|false
 * drawReturnUpside:true|false
 */
class Sinoptico
{
	//{container:div|window,onclick:calback,border:int,vehicleIcon:image}
	//
	constructor(params)
	//constructor(canvas,onclick,menu)
	{
		this.name=params.routeName;
		this.container=params.container;
		this.infoStop=(data) =>`<table class=\"table\"><th >${data.identifier}</th><tr><td> ${data.projection} </td></tr></table>`;
		this.infoVehicle=(data)=>`<table class=\"table\"><th >Móvil ${data.identifier}</th><tr><td> ${data.projection} </td></tr></table>`;
		if(params.infoStop!=undefined)
		{
			this.infoStop=params.infoStop;
			this.infoStop=this.infoStop.bind(this);
		}
		if(params.infoVehicle!=undefined)
		{
			this.infoVehicle=params.infoVehicle;
		}
		this.__showReturn=true;
		if(params.showReturn!=undefined)
			this.__showReturn=params.showReturn;
		//TEST:
		//this.__showReturn=true;
		
		this.toolTipDiv = document.createElement('div');
		this.menuDiv = document.createElement('div');
		this.menuDiv.className='menu-content';
		this.canvas = document.createElement('canvas');
		this.canvas.style.position = "absolute";
		this.canvas.border=0;
		if(params.border!=undefined)
		{
			this.canvas.border= params.border;
		}
		this.canvas.style.border   =this.canvas.border+"px solid";
		this.canvas.style.left="0";
		this.canvas.style.top="0";
		this.drawReturnUpside=true;
		if(params.vehicleAlternColorCallBack!=undefined)
			this.vehicleAlternColorCallBack=params.vehicleAlternColorCallBack;
		
		this.zoomFactor=1.0;
//		if(params.drawReturnUpside!=undefined)
//			this.drawReturnUpside=params.drawReturnUpside;
		this.stopImg = new Image();
		this.stopImg.width=10;
		this.stopImg.height=10;
		//this.patternType="circular";
		//this.patternType="linear";
		if(params.patternType!=undefined)
			this.patternType=params.patternType;
		//TEST:
		//this.patternType="linear";
		
		//this.canvas=canvas;
		if(params.routeName!=undefined)
		{
			
			this.title =  document.createElement('div');
			this.title.className='synopticTitle';
			this.title.innerHTML='<b>'+params.routeName+'</b>';
			this.container.appendChild(this.title);
			
		}

		this.container.appendChild(this.canvas);
		this.container.appendChild(this.toolTipDiv);
		this.container.appendChild(this.menuDiv);

		this.ctx= this.canvas.getContext('2d');
		// this.ctx.fillRect(0, 0,80, 98);
		//this.busPath=Path2d();

		this.vehicleIcon//= document.getElementById("vehicleIcon");
		=		new Image(); 
		this.vehicleIcon.addEventListener('load', function() {
			console.log("Image loaded");

		}, false);
//		this.vehicleIcon.addEventListener('load', function() {
//		// execute drawImage statements here
//		}, false);
		this.vehicleIcon.src="mybus.png";
		this.vehicleIcon.width=30;
		this.vehicleIcon.height=30;

		this.linewidth=0.9;//Porcentaje que ocupa del canvas
		//	var lineWidth=(canvas.width*linewidth);
		//this.margen=(canvas.width-lineWidth)/2.0;//MOVE TO POSITION OF THE LINE
		this.forwarLinePosition=80;
		
		
		if(this.patternType=="linear")
		{
			this.margin=40;
			this.returnLinePosition=150;
		}
		else
		{
			this.margin=60;
			this.returnLinePosition=300;
			if(this.container.height<this.returnLinePosition+this.vehicleIcon.height+50)
				this.container.height=this.returnLinePosition+this.vehicleIcon.height+50;
		}
	
		//this.separation=40;
		this.linecolour="#CD5F48";
		this.resize = this.resize.bind(this);
		this.init = this.init.bind(this);
		this.__handleMouseEvent = this.__handleMouseEvent.bind(this);
		
		this.__animateBus = this.__animateBus.bind(this);
		this.__showContexMenuFromMuli=this.__showContexMenuFromMuli.bind(this);
		this.__zoomIn=this.__zoomIn.bind(this);
		this.__zoomOut=this.__zoomOut.bind(this);
		this.mapBuses=new Map();
		this.buses=null;
		this.stops=null;
		this.resized=false;
		this.onVehiClick=params.onVehiClick;
		if(params.predictionFunction!=undefined)
			this.predictionFunction=params.predictionFunction;
		//	this.menu=menu;
		this.busDistanceToLine=7;
		this.textSeparation=6;
		this.maxBusLabelWidth=0;
		this.__mouseWheelHandler = this.__mouseWheelHandler.bind(this);
	}
	init()
	{
		window.addEventListener('resize',this.resize, false);
		this.canvas.addEventListener('click', this.__handleMouseEvent);
		this.canvas.addEventListener('mousedown', this.__handleMouseEvent);
		this.canvas.addEventListener('contextmenu', this.__handleMouseEvent);
		this.canvas.addEventListener('mousemove',this.__handleMouseEvent);
		this.canvas.addEventListener('wheel',this.__mouseWheelHandler);
		this.stopImg.onload = this.resize;
		//It has to be here for the first time it loads the image.
		this.stopImg.src = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABHNCSVQICAgIfAhkiAAAAAlwSFlz\nAAALEwAACxMBAJqcGAAAAPVJREFUOI2Vkl1qwlAQhT/z1GQpNYRQ29XUJYkiqOiDS7GldBENFUSx\ne2jjy/XhnluHGPJzYEgyOTNz5gfu8QRsgD1wkX0DayCv4f8jAbaAa7ENENcFf4rwC0yk5EE2AqbA\nnzgf1SSh8gl4bFCZAmdx17bnULkp2CYpFZOjnpxkd8VMMSvw03ZSYlEdnsWzfAX4NTn8sLomSOQr\nox6yaxEBR70Pe8QF7iEC3vTxWiENKmYx1nMHfhUOfyRph+oZtzVmwRlWeW5JkgE/4i7tjxh/nkHJ\nDL+qRPYCzE3ld+63RmyUNNmyLtgix19YoYol8AUsbM8BV0fAV591YB1RAAAAAElFTkSuQmCC\n';
		this.resize();
	}
	getVehicleIdentifier(id)
	{
		if(this.buses!=undefined)
		for(var m=0;m<this.buses.length;m++)
		{
			var bus=this.buses[m];
			if(bus.id==id)
			{
				return bus.identifier;
			}
		}
		return null;
	}
	__mouseWheelHandler(e) {
		
		// cross-browser wheel delta
		if(e.deltaY>0)
		{
			console.log(e);
			this.__zoomOut();
		}
		else
		{
			this.__zoomIn();
		}
	}
	//////////////////////////////////
	getElements(x,y)
	{
		var elements=[];
		for(var m=0;m<this.buses.length;m++)
		{
			if(this.buses[m].posX<=x && this.buses[m].posY<=y && this.buses[m].posX+this.vehicleIcon.width>=x &&  this.buses[m].posY+this.vehicleIcon.height>=y  )
				elements.push(this.buses[m]);
		}
		for(var m=0;m<this.stops.length;m++)
		{
			if(this.stops[m].posX<=x && this.stops[m].posY<=y && this.stops[m].posX+5>=x &&  this.stops[m].posY+5>=y  )
				elements.push(this.stops[m]);
		}
		return elements;
	}
	__hideContexMenu(  ){

		const menu = this.menuDiv;
		menu.style.visibility = "hidden";

	}
	__hideTooltip(  ){
		const tooltip=this.toolTipDiv;
		//	menu = window.getComputedStyle ? getComputedStyle(this.tootTipDiv, null) : this.tootTipDiv.currentStyle;

//		var menu = document.querySelector(".tooltip-right");
//		menu.style.visibility = "hidden";
//		menu.style.opacity = 0;
//		menu = document.querySelector(".tooltip-left");
		tooltip.style.visibility = "hidden";
		tooltip.style.opacity = 0;
	}
	__showTooltipBus(elements,{ top, left })
	{
		var leftScroll=0;
		if(this.container.scrollLeft!=undefined)
			leftScroll=this.container.scrollLeft;
		if(this.toolTipDiv.clientWidth!=undefined)
		{
			if(left+this.toolTipDiv.clientWidth<window.innerWidth+leftScroll)
			{

				this.toolTipDiv.className='tooltip-right';
				//if(elements[0].type=='bus')
				left+=this.vehicleIcon.width+10
			}
			else 
			{
				this.toolTipDiv.className="tooltip-left";
				//if(elements[0].type=='bus')
				left-=(this.toolTipDiv.clientWidth+10);
				//this.tootTipDiv.className='tooltip-left';
				//console.log(this.tootTipDiv);
				//console.log(menu);

			}
		}

		//menu = window.getComputedStyle ? getComputedStyle(this.tootTipDiv, null) : this.tootTipDiv.currentStyle;
		const tooltip=this.toolTipDiv;
		//const tooltip=document.getElementById("myToolTip");
		//tooltip.className='tooltip-right';
		//if(this.tootTipDiv.witdh)
		//const menu = document.querySelector(".tooltip");
		//const menu = document.querySelector(".tooltip-right");
		tooltip.style.left = `${left}px`;
		tooltip.style.top = `${top}px`;
		tooltip.style.visibility = "visible";
		tooltip.style.opacity = 1;

		//
		tooltip.innerHTML="";
		for(var i=0;i<elements.length;i++)
		{
			var data=elements[i];
			var table=`<table class=\"table\">
				<th >Móvil ${data.identifier}</th>
				<tr><td> ${data.projection} </td></tr>
				</table>`;
			tooltip.innerHTML+=this.infoVehicle(data);
		}
//		const toggleMenu = command => {
//		menu.style.display = command === "show" ? "block" : "none";
//		menuVisible = !menuVisible;
//		};


	}
	setPredictionsContent(content)
	{
		const tooltip=this.toolTipDiv;
		tooltip.innerHTML=content;
	}

	__showTooltipStop(elements,{ top, left })
	{
		
		this.toolTipDiv.className="tooltip-bottom";
		top+=15;
		var leftScroll=0;
		if(this.container.scrollLeft!=undefined)
			leftScroll=this.container.scrollLeft;
		if(this.toolTipDiv.clientWidth!=undefined)
		{
			//alert(left+ " "+(window.innerWidth+leftScroll)+ " "+window.innerWidth);
		
			left-=this.toolTipDiv.clientWidth/2;
			console.log((window.innerWidth+leftScroll)+ " LEFT "+(left+this.toolTipDiv.clientWidth));
			if(left<0)
			{
				this.toolTipDiv.className="tooltip-bottom-overflow-left";
				left+=this.toolTipDiv.clientWidth*0.40;

			}
			else if(left+this.toolTipDiv.clientWidth>window.innerWidth+leftScroll)
			{
				//alert(this.container.scrollLeft);
				this.toolTipDiv.className="tooltip-bottom-overflow-right";
				//left+=this.toolTipDiv.clientWidth*0.45;
				left-=this.toolTipDiv.clientWidth*0.40;
			}
		}

//		if(this.toolTipDiv.clientWidth!=undefined)
//		{
//		console.log("a");
//		if(left+this.toolTipDiv.clientWidth<window.innerWidth)
//		{

//		this.toolTipDiv.className='tooltip-right';
//		console.log("b");
//		if(elements[0].type=='bus')
//		left+=this.vehicleIcon.width+10
//		}
//		else 
//		{
//		this.toolTipDiv.className="tooltip-left";
//		console.log("c");
//		if(elements[0].type=='bus')
//		left-=(this.toolTipDiv.clientWidth+10);
//		//this.tootTipDiv.className='tooltip-left';
//		//console.log(this.tootTipDiv);
//		//console.log(menu);

//		}
//		}
		
		//menu = window.getComputedStyle ? getComputedStyle(this.tootTipDiv, null) : this.tootTipDiv.currentStyle;
		const tooltip=this.toolTipDiv;
		//const tooltip=document.getElementById("myToolTip");
		//tooltip.className='tooltip-right';
		//if(this.tootTipDiv.witdh)
		//const menu = document.querySelector(".tooltip");
		//const menu = document.querySelector(".tooltip-right");
		tooltip.style.left = `${left}px`;
		tooltip.style.top = `${top}px`;
		tooltip.style.visibility = "visible";
		tooltip.style.opacity = 1;

		//
		tooltip.innerHTML="";
		for(var i=0;i<elements.length;i++)
		{
			var data=elements[i];
			var table=this.infoStop(data);
			tooltip.innerHTML+=table;
		}
		this.predictionFunction(data.id);
//		const toggleMenu = command => {
//		menu.style.display = command === "show" ? "block" : "none";
//		menuVisible = !menuVisible;
//		};


	}

	__showTooltip(elements,{ top, left })
	{

		if(elements[0].type=='bus')
			this.__showTooltipBus(elements,{ top, left })
			else if (elements[0].type=='stop')
				this.__showTooltipStop(elements,{ top, left })
	}	
	__showContexMenuFromMuli(event)
	{
		console.log("__showContexMenuFromMuli");
		console.log(event);
//		{ top, left },indice
//		console.log(this.__menuElements);
//		console.log(indice);
		var top=event.target.top;
		var left= event.target.left;
		this.__showContexMenu([event.target.element],{  top,  left});
		
	}
	__showContexMenu(elements,{ top, left })
	{
	
		const menu = this.menuDiv;
		if(elements == null)
		{
			const menu = this.menuDiv;
			menu.innerHTML="";
			var a=document.createElement("a");
			a.innerHTML="Zoom in";
			a.addEventListener('click',this.__zoomIn)
		//	a.onclick=function (){this.__zoomIn};
			console.log("===>"+this.zoomFactor)
			menu.appendChild(a);
			a=document.createElement("a");
			a.innerHTML="Zoom out";
			a.addEventListener('click',this.__zoomOut)
		//	a.onclick=function (){this.__zoomIn};
			console.log("===>"+this.zoomFactor)
			menu.appendChild(a);
			menu.style.left = `${left}px`;
			menu.style.top = `${top}px`;
			menu.style.visibility = "visible";
			menu.style.border='1px solid';
			return;
		}
		if(elements[0].type=='bus')
		{
			top+=this.vehicleIcon.height+3;
		}
		else
			top+=3;
			
		if(left+menu.clientWidth>window.innerWidth)
		{
			left=window.innerWidth-menu.clientWidth;
			
		}
		menu.style.left = `${left}px`;
		menu.style.top = `${top}px`;
		menu.style.visibility = "visible";
		menu.style.border='1px solid';
		if(elements[0].type=='bus')
		{
			
			if(elements.length>1)
			{
				menu.innerHTML="";
				for(var pos=0;pos<elements.length;pos++)
				{
					var data=elements[pos];
					var a=document.createElement('a');
					a.innerHTML=data.identifier;
				
					menu.appendChild(a);
					a.element=elements[pos];
					a.addEventListener('click',this.__showContexMenuFromMuli)
					a.top=top-(this.vehicleIcon.height+3);
					a.left=left;
				//	MyObject.bind(this);
					//menu.innerHTML+=`<a onclick="doStuff(${pos}, ${top}, ${left})">${data.identifier}</a>`;
					;
				}
			}
			else
				{
				menu.innerHTML="<a>menu1</a><a>menu2</a>";
//				var a=document.createElement("a");
//				a.innerHTML="Zoom in";
//				a.addEventListener('click',this.__zoomIn)
//			//	a.onclick=function (){this.__zoomIn};
//				console.log("===>"+this.zoomFactor)
//				menu.appendChild(a);
//				a=document.createElement("a");
//				a.innerHTML="Zoom out";
//				a.addEventListener('click',this.__zoomOut)
//			//	a.onclick=function (){this.__zoomIn};
//				console.log("===>"+this.zoomFactor)
//				menu.appendChild(a);
				}
			
		}
		
		if(elements[0].type=='stop')
			menu.innerHTML="<a >stop</a>";
		



	}
	__zoomIn()
	{
		if(this.zoomFactor<4)
			this.zoomFactor+=0.1;
		this.resize();
		this.__hideContexMenu();
	}
	__zoomOut()
	{
		if(this.zoomFactor>1)
			this.zoomFactor-=0.1;
		this.resize();
		this.__hideContexMenu();
	}
	__handleMouseEvent(event)
	{
		var rect = this.canvas.getBoundingClientRect();	
		var x = event.pageX-rect.left,
		y = event.pageY-rect.top;
		if(event.type=="click")
		{
			this.__hideTooltip();
			this.__hideContexMenu();
			var elements=this.getElements(x,y);
			if(elements!=undefined && elements.length>0)
				this.onVehiClick(elements)
		}
		if(event.type=="contextmenu")
		{
			this.__hideTooltip();
			event.preventDefault();
			var elements=this.getElements(x,y);

			if(elements!=undefined && elements.length>0)
			{

				const origin = {
						left: elements[0].posX,
						top: elements[0].posY
				};
				this.__showContexMenu(elements,origin);
			}
			else
			{
				const origin = {
						left: x,
						top: y
				};
				this.__showContexMenu(null,origin);
				//this.__hideContexMenu();
			}
			//alert(event.type);
		}
		if(event.type=="mousemove")
		{
			var elements=this.getElements(x,y);

			if(elements!=undefined && elements.length>0)
			{
				const origin = {
						left: elements[0].posX,
						top: elements[0].posY
				};
				//console.log(origin);
				this.__showTooltip(elements,origin);
			}
			else
				this.__hideTooltip();

		}
		//console.log("CLICK "+x+ " "+y+ " canvas.width " +canvas.width+ " line "+lineWidth);




	}
	getLineWidth()
	{
		//return (this.canvas.width*this.linewidth);
		return (this.canvas.width-this.margin*2);
	}

	getLineMargin()
	{
		return (this.canvas.width-this.getLineWidth())/2.0;
	}
	
	drawLinearLine(direction)
	{
		this.ctx.save();
		this.ctx.beginPath();
		this.ctx.lineCap="round";
		this.ctx.moveTo(this.getLineMargin(), direction==0?this.forwarLinePosition:this.returnLinePosition);
		this.ctx.lineWidth = 5;
		this.ctx.strokeStyle = this.linecolour;
		this.ctx.lineTo(this.getLineWidth()+this.getLineMargin(), direction==0?this.forwarLinePosition:this.returnLinePosition);
		this.ctx.stroke();
		this.ctx.closePath();
		this.ctx.restore();
	}
	
	drawCircularLine()
	{
		this.drawLinearLine(0);
		this.ctx.save();
		this.ctx.beginPath();
		this.ctx.moveTo(this.getLineWidth()+this.getLineMargin(), this.forwarLinePosition);
		this.ctx.lineWidth = 5;
		this.ctx.strokeStyle = this.linecolour;
		this.ctx.lineTo(this.getLineWidth()+this.getLineMargin(), this.returnLinePosition);
		this.ctx.stroke();
		this.ctx.closePath();
		this.ctx.restore();
		this.drawLinearLine(1);
		this.ctx.save();
		this.ctx.beginPath();
		this.ctx.moveTo(this.getLineMargin(),this.returnLinePosition);
		this.ctx.lineWidth = 5;
		this.ctx.strokeStyle = this.linecolour;
		this.ctx.lineTo(this.getLineMargin(),this.forwarLinePosition);
		this.ctx.stroke();
		this.ctx.closePath();
		this.ctx.restore();
	}
	
	drawLine(direction)
	{
		if(this.patternType=="linear")
			this.drawLinearLine(direction);
		if(this.patternType=="circular" && direction==0)
			this.drawCircularLine();


	}
	resize() {
		console.log("RESIZE "+this.zoomFactor);
		var width=this.container.innerWidth;
		var height=this.container.innerHeight
		if(width == undefined)
		{
			width=this.container.clientWidth;
			height=this.container.clientHeight
		}
		width=this.zoomFactor*width;
		this.container.style.overflow="auto";  
		//console.log(this.canvas.style);
		if(this.canvas.border!= undefined)
		{

			width-=2*this.canvas.border;
			height-=2*this.canvas.border;
		}
		//console.log("resizing");
//		console.log("resizing"+ this.container.innerWidth);
//		console.log("resizing"+ this.container.clientWidth);
//		console.log("resizing"+ this.container.clientHeight);//offsetHeight==>incluye padding, scrollbar y bordes
		this.canvas.width = width;
		this.canvas.height = height;
		this.paint();
		this.resized=true;
	}
	getLastPostition(id)
	{

		var lastPosition={};
		if(this.buses == undefined)
			return lastPosition;

		for (var i=0; i < this.buses.length; i++) {
			if (this.buses[i].id == id) {
				//console.log("FOUND ID "+id);
				lastPosition.posX=this.buses[i].posX;
				lastPosition.posY=this.buses[i].posY;
				if(this.patternType=="linear")
					lastPosition.projected=this.buses[i].projection*this.getLineWidth();
				else if(this.patternType=="circular")
				{
					var h=this.returnLinePosition-this.forwarLinePosition;
					var lineLength=2*(h+this.getLineWidth());
					lastPosition.projected=this.buses[i].projection*lineLength;
				}
				return lastPosition;
			}

		}
		return lastPosition;
	}
	setBuses(buses)
	{
		//	console.log("SETTING BUSES");
		if(this.vehicleIcon.width==undefined || this.vehicleIcon.height==undefined)
		{
			this.vehicleIcon.height=this.vehicleIcon.naturalHeight;
			this.vehicleIcon.width=this.vehicleIcon.naturalWidth;
		}
		this.maxBusLabelWidth=this.vehicleIcon.width;
		for(var pos=0;pos<buses.length;pos++)
		{
			var bus=buses[pos];
			//console.log(this.vehicleIcon);
			if(bus.direction==undefined)
				bus.direction=0;
			
			if(this.ctx.measureText(bus.identifier).width>this.maxBusLabelWidth)
				this.maxBusLabelWidth=this.ctx.measureText(bus.identifier).width;
			
//			this.vehicleIcon.width=this.vehicleIcon.naturalWidth;
//			this.vehicleIcon.height=this.vehicleIcon.naturalHeight;
			//console.log(this.vehicleIcon.height+ " x "+	this.vehicleIcon.width);
			
			
			bus.lastPosition=this.getLastPostition(bus.id);
			bus.type='bus';

		}
		this.buses=buses;
		//this.paint();
	}
	animateBus(steps)
	{
		this.counter=0;
		this.steps=steps;
		window.requestAnimationFrame(this.__animateBus);
	}
	__getXYPositionByPixelDistance(distance)
	{
		var _object={};
		var _objWidth=this.vehicleIcon.width;
		var _objHeight=this.vehicleIcon.height;
		var distanceToLine=this.busDistanceToLine;
		var h=this.returnLinePosition-this.forwarLinePosition;
		var lineLength=2*(h+this.getLineWidth());
		//console.log("distance "+distance);
		if(distance<this.getLineWidth())
		{
		//	console.log("CASE 1");
			_object.posX = distance+this.getLineMargin()-_objWidth/2;
			_object.posY =this.forwarLinePosition-(_objHeight)-distanceToLine;
			_object.labelYPosition=_object.posY-this.textSeparation;
		}
		else if(distance>= this.getLineWidth() && distance<this.getLineWidth() +h)
		{
			//We could change icon because of the arrow.
			//console.log("CASE 2");
			_object.posX = this.getLineWidth()+distanceToLine+this.getLineMargin();
			_object.posY = this.forwarLinePosition+(distance-this.getLineWidth()) -_objHeight/2;	
			_object.labelYPosition=_object.posY-this.textSeparation;
		}
		else if(distance>= this.getLineWidth()+h && distance<2*this.getLineWidth()+h)
		{
			//TODO: THIS NEEDS TO REDRAW THE LINE
//			if(this.drawReturnUpside==true) 
//				_object.posY =this.returnLinePosition-this.vehicleIcon.height-this.busDistanceToLine;
//			else
			//console.log("CASE 3");
				_object.posY = this.returnLinePosition+(distanceToLine);
				_object.posX =this.getLineWidth()-(distance-(this.getLineWidth()+h))+this.getLineMargin()-_objWidth/2;
				_object.labelYPosition=_object.posY+this.textSeparation+_objHeight;
	
		}
		else
		{
	//		console.log("CASE 4");
			_object.posX = this.getLineMargin()-(distanceToLine+(this.maxBusLabelWidth));
			_object.posY = this.returnLinePosition-(distance-(2*this.getLineWidth()+h)) -(_objHeight/2);
			_object.labelYPosition=_object.posY-this.textSeparation;
		}
		//console.log(_object.posX + " ,"+_object.posY );
		return _object;
	}
	//TODO: Borrar bien los buses
	__animateBus()
	{
		//Clear forward
		this.ctx.save();
		//console.log(counter);
		this.ctx.font="10px Georgia";
		var metrics=this.ctx.measureText("anyText");
		metrics.height=parseInt(this.ctx.font.match(/\d+/), 10);
		
		this.ctx.clearRect(0,0,this.getLineMargin()-this.busDistanceToLine+1,this.canvas.height);
		this.ctx.clearRect(this.canvas.width-this.getLineMargin()+this.busDistanceToLine,0,this.getLineMargin(),this.canvas.height);
		this.ctx.clearRect(0, this.forwarLinePosition-(this.vehicleIcon.height+metrics.height+this.textSeparation+this.busDistanceToLine+1), this.canvas.width, (this.vehicleIcon.height+metrics.height+this.textSeparation)+1);//Tenemos que tener el cuadrado de los buses.
		if(this.drawReturnUpside==true && this.patternType=="linear")//TODO: If it is circular, we should repaint line
			this.ctx.clearRect( 0,this.returnLinePosition-(this.vehicleIcon.height+metrics.height+this.textSeparation+this.busDistanceToLine+1), this.canvas.width,(this.vehicleIcon.height+metrics.height+this.textSeparation)+1);//Tenemos que tener el cuadrado de los buses.
		else 
			this.ctx.clearRect( 0,this.returnLinePosition+this.busDistanceToLine, this.canvas.width, (this.vehicleIcon.height+metrics.height+this.textSeparation));//Tenemos que tener el cuadrado de los buses.
		
		for(var pos=0;pos<this.buses.length;pos++)
		{
			var bus=this.buses[pos];
			this.setObjectPosition(bus);
//			bus.posX = (bus.direction==0)? (this.getLineWidth()*bus.projection)+this.getLineMargin()-this.vehicleIcon.width/2:(this.getLineWidth()-(this.getLineWidth()*bus.projection))+this.getLineMargin()-this.vehicleIcon.width/2;
//			if(this.drawReturnUpside==true)
//				bus.posY = (bus.direction==0)? this.forwarLinePosition-this.vehicleIcon.height-this.busDistanceToLine:this.returnLinePosition-this.vehicleIcon.height-this.busDistanceToLine;
//			else
//				bus.posY = (bus.direction==0)? this.forwarLinePosition-this.vehicleIcon.height-this.busDistanceToLine:this.returnLinePosition+this.busDistanceToLine;

			var xToDraw=bus.posX;
			var yToDraw=bus.posY;


			if(bus.lastPosition!=undefined && bus.lastPosition.posX!=undefined  )
			{
				if(this.patternType=="linear")
				{
					if(bus.lastPosition.posX>bus.posX)
						xToDraw=bus.lastPosition.posX-(bus.lastPosition.posX-bus.posX)/this.steps*this.counter;
					else
						xToDraw=bus.lastPosition.posX+((bus.posX-bus.lastPosition.posX)/this.steps)*this.counter;
				}
				else if(this.patternType=="circular" &&  bus.lastPosition.projected!=undefined)
				{
					var distance=bus.lastPosition.projected+((bus.projected-bus.lastPosition.projected)/this.steps)*this.counter;
					var positionStep=this.__getXYPositionByPixelDistance(distance);
					xToDraw=positionStep.posX;
					yToDraw=positionStep.posY;
					bus.labelYPosition=positionStep.labelYPosition;
				}
			}
			this.ctx.fillStyle = bus.fill;
			//	this.ctx.fillRect(xToDraw, yToDraw, this.vehicleIcon.width, this.vehicleIcon.height);
		//	this.ctx.drawImage(this.vehicleIcon,xToDraw, yToDraw,this.vehicleIcon.width, this.vehicleIcon.height);
			if(this.vehicleAlternColorCallBack==undefined)
				this.ctx.drawImage(this.vehicleIcon,xToDraw, yToDraw,this.vehicleIcon.width, this.vehicleIcon.height);
			else
				this.ctx.drawImage(this.vehicleAlternColorCallBack(bus),xToDraw, yToDraw,this.vehicleIcon.width, this.vehicleIcon.height);

			var fontPostY=0;
			//console.log(metrics);
			if(this.patternType=="linear")
				 fontPostY=(bus.direction==0)?bus.posY-this.textSeparation:(this.drawReturnUpside==true)?bus.posY-this.textSeparation:bus.posY+metrics.height+this.vehicleIcon.height+this.textSeparation;
			else
				 fontPostY=bus.labelYPosition;
			//console.log("fontPostY="+fontPostY);
			this.ctx.fillText(bus.identifier,xToDraw,fontPostY );

//			ctx.stroke();
		}
		this.ctx.restore();
		this.counter++;
		if(this.counter<=this.steps)
		{
			//console.log("TIME "+ this.counter);
			window.requestAnimationFrame(this.__animateBus);
		}
	}
	setStops(stops)
	{
		console.log('setting stops'+stops.length);
		for(var pos=0;pos<stops.length;pos++)
		{
			console.log('setting stops'+pos);
			stops[pos].type='stop';
		}
		this.stops=stops;
		this.paintStop();
		//console.log('setting stops'+this.stops.length);
	}
	paintStop()
	{	
		if(this.stops==undefined || this.stops==null)
			return;
		this.ctx.save();
		for(var pos=0;pos<this.stops.length;pos++)
		{
			var stop=this.stops[pos];
			if(this.__showReturn!= undefined && this.__showReturn==false && stop.direction==1)
				continue;
			this.setObjectPosition(stop);
			//console.log("PRE: "+stop.posX+ " "+stop.posY);
//			stop.posX = (stop.direction==0)? (this.getLineWidth()*stop.projection)+this.getLineMargin():(this.getLineWidth()-(this.getLineWidth()*stop.projection))+this.getLineMargin();
//			stop.posY = (stop.direction==0)? this.forwarLinePosition:this.returnLinePosition;
		/*	this.ctx.lineWidth = 1;
			this.ctx.beginPath();
			this.ctx.arc(stop.posX, stop.posY, 4, 0, Math.PI*2, true); 
			this.ctx.closePath();
			this.ctx.fill();*/
			
			this.ctx.drawImage(this.stopImg,stop.posX-this.stopImg.width/2, stop.posY-this.stopImg.height/2,this.stopImg.width, this.stopImg.height);
		}
		this.ctx.restore();
	}
	
	setObjectPosition(_object)
	{
		var _objWidth=this.vehicleIcon.width;
		var _objHeight=this.vehicleIcon.height;
		var distanceToLine=this.busDistanceToLine;
		if(_object.type=="stop")
		{
			_objWidth=this.stopImg.width;
			_objHeight=this.stopImg.height;
			distanceToLine=0;
			
		}
	//	console.log("_objWidth "+ _objWidth+ " _objHeight: "+ _objHeight+ " _object.type "+_object.type);
		if(this.patternType=="linear")
		{
			console.log("LINEAR");
			_object.posX = (_object.direction==0)? (this.getLineWidth()*_object.projection)+this.getLineMargin()-_objWidth/2:(this.getLineWidth()-(this.getLineWidth()*_object.projection))+this.getLineMargin()-_objWidth/2;
			if(this.drawReturnUpside==true)
				_object.posY = (_object.direction==0)? this.forwarLinePosition-((_object.type=="stop")?0:_objHeight)-distanceToLine:this.returnLinePosition-((_object.type=="stop")?0:_objHeight)-distanceToLine;
			else
				_object.posY = (_object.direction==0)? this.forwarLinePosition-((_object.type=="stop")?0:_objHeight)-distanceToLine:this.returnLinePosition+((_object.type=="stop")?0:this.separation-_objHeight);
			_object.projected=this.getLineWidth()*_object.projection;
			if(_object.posX>this.getLineWidth())
			{
				console.warn("object projection>1");
				_object.posX=this.getLineWidth();
			}
		}
		else if(this.patternType=="circular")
		{
		//	console.log("CIRCULAR");
			var h=this.returnLinePosition-this.forwarLinePosition;
			var lineLength=2*(h+this.getLineWidth());
			var busLinePosition=lineLength*_object.projection;
			_object.projected=busLinePosition;
			if(busLinePosition>lineLength)
			{
				console.warn("object projection>1");
				busLinePosition=lineLength;
			}
			
			if(busLinePosition<this.getLineWidth())
			{
			//	console.log("CASE 1");
				_object.posX = busLinePosition+this.getLineMargin()-_objWidth/2;
				_object.posY =this.forwarLinePosition-((_object.type=="stop")?0:_objHeight)-distanceToLine;
				if(_object.type=='bus')
					_object.labelYPosition=_object.posY-this.textSeparation;
			}
			else if(busLinePosition>= this.getLineWidth() && busLinePosition<this.getLineWidth() +h)
			{
				//We could change icon because of the arrow.
				//console.log("CASE 2");
				_object.posX = this.getLineWidth()+distanceToLine+this.getLineMargin();
				_object.posY = this.forwarLinePosition+(busLinePosition-this.getLineWidth()) -_objHeight/2;		
				if(_object.type=='bus')
					_object.labelYPosition=_object.posY-this.textSeparation;
			}
			else if(busLinePosition>= this.getLineWidth()+h && busLinePosition<2*this.getLineWidth()+h)
			{
				//TODO: THIS NEEDS TO REDRAW THE LINE
//				if(this.drawReturnUpside==true) 
//					_object.posY =this.returnLinePosition-this.vehicleIcon.height-this.busDistanceToLine;
//				else
			//	console.log("CASE 3");
					_object.posY = this.returnLinePosition+((_object.type=="stop")?0:distanceToLine);
					_object.posX =this.getLineWidth()-(busLinePosition-(this.getLineWidth()+h))+this.getLineMargin()-_objWidth/2;
					if(_object.type=='bus')
						_object.labelYPosition=_object.posY+this.textSeparation+_objHeight;
		
			}
			else
			{
		//		console.log("CASE 4");
				_object.posX = this.getLineMargin()-(distanceToLine+((_object.type=="stop")?0:this.maxBusLabelWidth));
				_object.posY = this.returnLinePosition-(busLinePosition-(2*this.getLineWidth()+h)) -((_object.type=="stop")?0:_objHeight/2);
				if(_object.type=='bus')
					_object.labelYPosition=_object.posY-this.textSeparation;
			}
			
		}
	
	}
	
	paintBus()
	{
		//this.repaint();
		this.ctx.save();
		//console.log(counter);
		this.ctx.font="10px Georgia";
		var metrics=this.ctx.measureText("anyText");
		metrics.height=parseInt(this.ctx.font.match(/\d+/), 10);
		this.ctx.clearRect(this.getLineMargin()+2.5, this.forwarLinePosition-(this.vehicleIcon.height+metrics.height+this.textSeparation+this.busDistanceToLine+1), this.canvas.width-2*this.getLineMargin()-5, (this.vehicleIcon.height+metrics.height+this.textSeparation));//Tenemos que tener el cuadrado de los buses.
		
		//this.ctx.clearRect(0, this.forwarLinePosition-this.vehicleIcon.height-metrics.height-this.textSeparation, this.canvas.width, this.vehicleIcon.height+metrics.height+this.textSeparation-3);//Tenemos que tener el cuadrado de los buses.
		if(this.drawReturnUpside==true && this.patternType=="linear")
			this.ctx.clearRect( this.getLineMargin()+2.5,this.returnLinePosition-(this.vehicleIcon.height+metrics.height+this.textSeparation+this.busDistanceToLine+1), this.canvas.width-2*this.getLineMargin()-5,  (this.vehicleIcon.height+metrics.height+this.textSeparation));//Tenemos que tener el cuadrado de los buses.
		else 
			this.ctx.clearRect( this.getLineMargin()+2.5,this.returnLinePosition+this.busDistanceToLine, this.canvas.width-2*this.getLineMargin()-5, (this.vehicleIcon.height+metrics.height+this.textSeparation));//Tenemos que tener el cuadrado de los buses.
	
		for(var pos=0;pos<this.buses.length;pos++)
		{
			var bus=this.buses[pos];

			this.setObjectPosition(bus);
			var xToDraw=bus.posX;
			var yToDraw=bus.posY;


			/*if(bus.lastPosition!=undefined && bus.lastPosition.posX!=undefined  )
			{

				if(bus.lastPosition.posX>bus.posX)
					xToDraw=bus.lastPosition.posX-(bus.lastPosition.posX-bus.posX)/this.steps*this.counter;
				else
					xToDraw=bus.lastPosition.posX+((bus.posX-bus.lastPosition.posX)/this.steps)*this.counter;

			}*/
			this.ctx.fillStyle = bus.fill;
			//	this.ctx.fillRect(xToDraw, yToDraw, this.vehicleIcon.width, this.vehicleIcon.height);
			if(this.vehicleAlternColorCallBack==undefined)
				this.ctx.drawImage(this.vehicleIcon,xToDraw, yToDraw,this.vehicleIcon.width, this.vehicleIcon.height);
			else
				this.ctx.drawImage(this.vehicleAlternColorCallBack(bus),xToDraw, yToDraw,this.vehicleIcon.width, this.vehicleIcon.height);


			//console.log(metrics);
//			var fontPostY=(bus.direction==0)?bus.posY-this.textSeparation:(this.drawReturnUpside==true)?bus.posY-this.textSeparation:bus.posY+metrics.height+this.vehicleIcon.height+this.textSeparation;
//			this.ctx.fillText(bus.identifier,xToDraw,fontPostY );
			var fontPostY=0;
			//console.log(metrics);
			if(this.patternType=="linear")
				 fontPostY=(bus.direction==0)?bus.posY-this.textSeparation:(this.drawReturnUpside==true)?bus.posY-this.textSeparation:bus.posY+metrics.height+this.vehicleIcon.height+this.textSeparation;
			else
				 fontPostY=bus.labelYPosition;
			console.log("fontPostY="+fontPostY);
			this.ctx.fillText(bus.identifier,xToDraw,fontPostY );
//			ctx.stroke();
		}
		this.ctx.restore();

	}
	repaint()
	{
		this.drawLine(0);
		if(this.__showReturn)
			this.drawLine(1);
		this.paintStop();
	}
	paint()
	{
		if(this.resize)
		{
			this.repaint();
		}
		if(this.buses== undefined || this.buses==null)	
		{
			console.warn("no bus to paint");
			return;
		}
		this.paintBus();
		this.resized=false;
		//
		//this.ctx.translate(25,20);
	}
	draw()
	{
		this.ctx.save();
		this.ctx.fillStyle = 'rgb(255,255,0)';

		this.ctx.translate(40,40);
		this.ctx.fillRect(0,0,25,25);
		this.ctx.restore();
		this.ctx.save();
		this.ctx.fillStyle = 'rgb(255,0,0)';

		//this.ctx.restore();
		//
		this.ctx.translate(40,40);
		this.ctx.drawImage(this.vehicleIcon,0,0);
		this.ctx.fillRect(5,5,5,5);
		this.ctx.restore();


	}

}



function myFunction()
{



	var buses=[{
		id:33,		
		width: 20,
		height: 10,
		direction: 0,
		projection: Math.random(),
		identifier:"ppu-4111",
		fill: "#444444",
		isDragging: false
	},{
		id:23,		
		width: 20,
		height: 10,
		direction: 0,
		projection:Math.random(),
		identifier:"ppu-4211",
		fill: "#444444",
		isDragging: false,
		isScheduledService: false,
		freqStartTime:1537843461864,
	},{
		id:1,		
		width: 20,
		height: 10,
		direction: 1,
		projection:Math.random(),
		identifier:"ppu-4311",
		fill: "#444444",
		isDragging: false
	}];
	synoptic.setBuses(buses);
	synoptic.steps=100;
	synoptic.counter=0;
	console.log("llamando a paint bus");
	//window.requestAnimationFrame(test.animateBus);
	synoptic.animateBus(20);
//	test.paintBus();
}
