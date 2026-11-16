package com.citytycoon

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

private data class Plot(val x:Int,val y:Int,val kind:String)
private data class Spec(val name:String,val group:String,val cost:Int,val upkeep:Int=0,val income:Int=0,val homes:Int=0,val jobs:Int=0,val pollution:Int=0,val joy:Int=0,val level:Int=1)
private val catalog=listOf(
 Spec("Road","Roads",35,1),Spec("Small House","Residential",350,4,homes=12),Spec("Family House","Residential",700,8,homes=28,level=2),Spec("Apartment","Residential",1800,22,homes=90,level=3),Spec("Skyscraper","Residential",9000,100,homes=450,level=6),
 Spec("Small Shop","Commercial",650,12,65,jobs=8),Spec("Supermarket","Commercial",1700,30,180,jobs=24,level=3),Spec("Restaurant","Commercial",1100,22,125,jobs=14,joy=2,level=2),Spec("Office","Commercial",2600,48,260,jobs=50,level=4),
 Spec("Factory","Industrial",1200,35,190,jobs=35,pollution=9),Spec("Warehouse","Industrial",1900,42,150,jobs=18,pollution=3,level=2),Spec("Industrial Complex","Industrial",6500,125,620,jobs=100,pollution=22,level=5),
 Spec("Park","Decorations",300,5,joy=7),Spec("Tree","Decorations",90,1,joy=2),Spec("Fountain","Decorations",500,8,joy=5,level=2),
 Spec("Police Station","Services",1800,55,joy=5,level=2),Spec("Fire Station","Services",1600,48,joy=3,level=2),Spec("Hospital","Services",3200,95,joy=9,level=3),Spec("School","Services",2200,65,joy=6,level=3),Spec("University","Services",5200,125,joy=9,level=5)
)
private val groups=listOf("Roads","Residential","Commercial","Industrial","Services","Decorations")
class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState); @Suppress("DEPRECATION") window.decorView.systemUiVisibility=5894 or 1024 or 512; setContent{CityTycoonApp()}}
}
@Composable private fun CityTycoonApp(){
 val ctx= LocalContext.current; val focus= remember{FocusRequester()}
 var started by remember{mutableStateOf(false)}; var cash by remember{mutableIntStateOf(12000)}; var day by remember{mutableIntStateOf(1)}
 var tax by remember{mutableIntStateOf(10)}; var happy by remember{mutableIntStateOf(65)}; var radius by remember{mutableIntStateOf(3)}
 var speed by remember{mutableIntStateOf(1)}; var paused by remember{mutableStateOf(false)}
 var tool by remember{mutableStateOf("Select")}; var group by remember{mutableStateOf("Residential")}; var tab by remember{mutableStateOf("Build")}
 var camera by remember{mutableStateOf(Offset.Zero)}; var zoom by remember{mutableFloatStateOf(1f)}; var selected by remember{mutableStateOf<Plot?>(null)}
 var note by remember{mutableStateOf("Welcome, Mayor! Start by building roads and homes.")}; val plots= remember{mutableStateListOf<Plot>()}
 fun spec(k:String)=catalog.firstOrNull{it.name==k}
 fun pop()=plots.sumOf{spec(it.kind)?.homes?:0}.let{(it*.72).toInt()}
 fun jobs()=plots.sumOf{spec(it.kind)?.jobs?:0}
 fun level()=1+pop()/100
 fun save(){runCatching{val j=JSONObject().put("cash",cash).put("day",day).put("tax",tax).put("happy",happy).put("radius",radius);val a=JSONArray();plots.forEach{a.put(JSONObject().put("x",it.x).put("y",it.y).put("kind",it.kind))};j.put("plots",a);ctx.getSharedPreferences("city",Context.MODE_PRIVATE).edit().putString("save",j.toString()).apply()}}
 fun load():Boolean=runCatching{val j=JSONObject(ctx.getSharedPreferences("city",0).getString("save",null)?:return false);cash=j.optInt("cash",12000);day=j.optInt("day",1);tax=j.optInt("tax",10);happy=j.optInt("happy",65);radius=j.optInt("radius",3).coerceIn(2,5);plots.clear();val a=j.optJSONArray("plots")?:JSONArray();for(i in 0 until a.length()){val p=a.getJSONObject(i);plots.add(Plot(p.getInt("x"),p.getInt("y"),p.getString("kind")))};true}.getOrDefault(false)
 fun reset(){plots.clear();cash=12000;day=1;tax=10;happy=65;radius=3;camera=Offset.Zero;zoom=1f;selected=null;save()}
 fun tapTile(x:Int,y:Int){
  if(tool=="Expand"){val cost=2500+(radius-2)*1800;if(kotlin.math.abs(x)>radius+1||kotlin.math.abs(y)>radius+1||(kotlin.math.abs(x)<=radius&&kotlin.math.abs(y)<=radius)){note="Choose a tile beside owned land.";return};if(cash<cost||radius>=5){note="Expansion costs \\$cost.";return};cash-=cost;radius++;tool="Select";note="Land expanded!";save();return}
  val old=plots.firstOrNull{it.x==x&&it.y==y};if(tool=="Select"){selected=old;return};val s=spec(tool)?:return
  if(old!=null){note="Tile occupied.";return};if(kotlin.math.abs(x)>radius||kotlin.math.abs(y)>radius){note="Locked land. Use Expand.";return}
  if(level()<s.level){note="Unlocks at city level ${s.level}.";return};if(cash<s.cost){note="Not enough cash.";return}
  cash-=s.cost;plots.add(Plot(x,y,s.name));selected=plots.last();note="${s.name} constructed.";save()
 }
 LaunchedEffect(started,paused,speed){while(started&&!paused){delay((4500L/speed).coerceAtLeast(900));day++;val h=plots.sumOf{spec(it.kind)?.homes?:0};val taxMoney=(h*.72*tax/100).toInt();val business=plots.sumOf{spec(it.kind)?.income?:0};val upkeep=plots.sumOf{spec(it.kind)?.upkeep?:0};val services=plots.count{spec(it.kind)?.group=="Services"};val joy=plots.sumOf{spec(it.kind)?.joy?:0};val dirty=plots.sumOf{spec(it.kind)?.pollution?:0};happy=(58+joy+services*2-dirty-max(0,tax-12)*2).coerceIn(5,100);val net=taxMoney+business-upkeep;cash+=net;note="Day $day • Daily balance ${if(net>=0)"+" else ""}\\$net";if(cash<0)note="Bankruptcy warning! Reduce upkeep or grow income.";save()}}
 if(!started){MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xFFD5B56D),background=Color(0xFF17221D))){Box(Modifier.fillMaxSize().background(Color(0xFF17221D)),Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)){Text("CITY TYCOON",style=MaterialTheme.typography.displayLarge,color=Color(0xFFE4C77F),fontWeight=FontWeight.Black);Text("Build a town. Shape a city.",color=Color.LightGray);Button(onClick={reset();started=true}){Text("NEW CITY")};OutlinedButton(onClick={if(load())started=true else note="No saved city yet."}){Text("CONTINUE CITY")};Text(note,color=Color.Gray)}}};return}
 val population=pop();val dailyIncome=plots.sumOf{spec(it.kind)?.income?:0}+(population*.72*tax/100).toInt();val dailyCost=plots.sumOf{spec(it.kind)?.upkeep?:0}
 MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xFFD5B56D),background=Color(0xFF17221D),surface=Color(0xFF26342C))){
 Column(Modifier.fillMaxSize().background(Color(0xFF17221D)).focusRequester(focus).focusable().onPreviewKeyEvent{e->if(e.type!=KeyEventType.KeyDown)false else when(e.key){Key.W,Key.DirectionUp->{camera+=Offset(0f,35f);true};Key.S,Key.DirectionDown->{camera+=Offset(0f,-35f);true};Key.A,Key.DirectionLeft->{camera+=Offset(35f,0f);true};Key.D,Key.DirectionRight->{camera+=Offset(-35f,0f);true};Key.B->{tab="Build";true};Key.R->{tool="Road";tab="Build";true};Key.Spacebar->{paused=!paused;true};Key.Escape->{tool="Select";selected=null;true};Key.One->{speed=1;paused=false;true};Key.Two->{speed=2;paused=false;true};Key.Three->{speed=3;paused=false;true};else->false}}){
 Row(Modifier.fillMaxWidth().background(Color(0xFF202D26)).padding(9.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
 Column{Text("CITY TYCOON",color=Color(0xFFE4C77F),fontWeight=FontWeight.Bold);Text("DAY $day • LEVEL ${level()}",color=Color.LightGray,style=MaterialTheme.typography.labelSmall)}
 Stat("CASH","$"+ "%,d".format(cash));Stat("POPULATION","%,d".format(population));Stat("HAPPINESS","$happy%");Stat("JOBS","%,d".format(jobs()))
 TextButton(onClick={paused=!paused}){Text(if(paused)"▶" else "Ⅱ")};TextButton(onClick={speed=if(speed==3)1 else speed+1;paused=false}){Text("${speed}x")};TextButton(onClick={save();note="City saved."}){Text("SAVE")};TextButton(onClick={started=false}){Text("MENU")}
 }
 Row(Modifier.weight(1f).fillMaxWidth()){
 Column(Modifier.width(160.dp).fillMaxHeight().background(Color(0xFF26342C)).padding(7.dp)){
 Row{listOf("Build","Stats","Land").forEach{t->TextButton(onClick={tab=t;if(t!="Build")tool="Select"},contentPadding=PaddingValues(4.dp)){Text(t,color=if(tab==t)Color(0xFFE4C77F)else Color.LightGray)}}}
 if(tab=="Build"){
 groups.forEach{g->TextButton(onClick={group=g},contentPadding=PaddingValues(2.dp)){Text((if(group==g)"› " else "")+g,color=if(group==g)Color(0xFFE4C77F)else Color.LightGray)}}
 Divider(color=Color.Gray);Column(Modifier.weight(1f).verticalScroll(rememberScrollState())){catalog.filter{it.group==group}.forEach{s->TextButton(onClick={tool=s.name;note="Selected ${s.name} • \\${s.cost}"},contentPadding=PaddingValues(3.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.fillMaxWidth()){Text(s.name,color=if(tool==s.name)Color(0xFFE4C77F)else Color.White);Text("\\${s.cost} • upkeep \\${s.upkeep}",color=Color.Gray,style=MaterialTheme.typography.labelSmall)}}}}
 }else if(tab=="Stats"){
 Text("CITY REPORT",color=Color(0xFFE4C77F),fontWeight=FontWeight.Bold);Text("Housing capacity: ${plots.sumOf{spec(it.kind)?.homes?:0}}",color=Color.White);Text("Jobs: ${jobs()}",color=Color.White);Text("Daily income: \\$dailyIncome",color=Color.White);Text("Daily expenses: \\$dailyCost",color=Color.White);Text("Tax rate: $tax%",color=Color.White);Slider(value=tax.toFloat(),onValueChange={tax=it.toInt()},valueRange=5f..20f)
 }else{Text("LAND EXPANSION",color=Color(0xFFE4C77F));Text("Owned area: ${radius*2+1} × ${radius*2+1}",color=Color.White);val price=2500+(radius-2)*1800;Text("Next plot: $$price",color=Color.White);Button(onClick={if(radius<5&&cash>=price){cash-=price;radius++;save();note="Land expanded."}else note="Cannot expand yet."},enabled=radius<5){Text("BUY LAND")}}
 Spacer(Modifier.weight(1f));Text("Tool: $tool",color=Color(0xFFE4C77F),style=MaterialTheme.typography.labelSmall);Text("WASD • B build • R road • Space pause",color=Color.Gray,style=MaterialTheme.typography.labelSmall)
 }
 Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFF8CA978))){
 Canvas(Modifier.fillMaxSize().pointerInput(tool,camera,zoom,radius,plots.size){detectTapGestures{p->val cell=48f*zoom;val c=Offset(size.width/2+camera.x,size.height/2+camera.y);tapTile(floor((p.x-c.x)/cell+.5f).toInt(),floor((p.y-c.y)/cell+.5f).toInt())}}.pointerInput(Unit){detectTransformGestures{_,pan,scale,_->camera+=pan;zoom=(zoom*scale).coerceIn(.55f,1.8f)}}){
 drawRect(Color(0xFF8CA978));val cell=48f*zoom;val c=Offset(size.width/2+camera.x,size.height/2+camera.y);val range=(max(size.width,size.height)/cell).toInt()+3
 for(x in -range..range)for(y in -range..range){val p=Offset(c.x+x*cell-cell/2,c.y+y*cell-cell/2);val own=kotlin.math.abs(x)<=radius&&kotlin.math.abs(y)<=radius;drawRect(if(own)Color(0x2280A06B)else Color(0xAA46564C),p,androidx.compose.ui.geometry.Size(cell-1,cell-1));drawRect(if(own)Color(0x33708058)else Color(0x668C9A8D),p,androidx.compose.ui.geometry.Size(cell-1,cell-1),style=Stroke(1f))}
 plots.forEach{b->val x=c.x+b.x*cell;val y=c.y+b.y*cell;val color=when(spec(b.kind)?.group){"Roads"->Color(0xFF596269);"Residential"->Color(0xFFE5D2AE);"Commercial"->Color(0xFFB99BC5);"Industrial"->Color(0xFF9C7770);"Decorations"->Color(0xFF397B4C);else->Color(0xFFB6B6A0)};drawRoundRect(color,Offset(x-cell*.43f,y-cell*.43f),androidx.compose.ui.geometry.Size(cell*.86f,cell*.86f),androidx.compose.ui.geometry.CornerRadius(4f));if(b.kind=="Road"){drawLine(Color(0xFFE3D7A4),Offset(x-cell*.4f,y),Offset(x+cell*.4f,y),2f*zoom)}else{drawRect(Color(0xFF414B47),Offset(x-cell*.18f,y-cell*.28f),androidx.compose.ui.geometry.Size(cell*.36f,cell*.12f));drawRect(Color(0xFF7B624E),Offset(x-cell*.09f,y+cell*.2f),androidx.compose.ui.geometry.Size(cell*.18f,cell*.2f))};if(selected==b)drawRoundRect(Color.Yellow,Offset(x-cell*.48f,y-cell*.48f),androidx.compose.ui.geometry.Size(cell*.96f,cell*.96f),style=Stroke(3f))}
 }
 Surface(Modifier.align(Alignment.BottomStart).padding(9.dp),color=Color(0xDD26342C)){Text(note,Modifier.padding(8.dp),color=Color.White,style=MaterialTheme.typography.labelSmall)}
 selected?.let{b->Surface(Modifier.align(Alignment.BottomEnd).padding(9.dp),color=Color(0xF026342C)){Column(Modifier.padding(8.dp)){Text(b.kind,color=Color(0xFFE4C77F));Text("Tile ${b.x}, ${b.y}",color=Color.White);Text("Upkeep: \\${spec(b.kind)?.upkeep?:0}/day",color=Color.LightGray);TextButton(onClick={cash+=(spec(b.kind)?.cost?:0)/2;plots.remove(b);selected=null;save();note="Demolished • 50% refund"}){Text("DEMOLISH")}}}}
 }
 }
 Row(Modifier.fillMaxWidth().background(Color(0xFF202D26)).padding(7.dp),horizontalArrangement=Arrangement.SpaceBetween){Text("Daily: +\\$dailyIncome / -\\$dailyCost",color=Color.LightGray,style=MaterialTheme.typography.labelSmall);Text("Tap build • drag pan • pinch zoom • keyboard/mouse",color=Color(0xFFE4C77F),style=MaterialTheme.typography.labelSmall)}
 }
 }
 LaunchedEffect(Unit){focus.requestFocus()}
}
@Composable private fun Stat(label:String,value:String){Column{Text(label,color=Color.Gray,style=MaterialTheme.typography.labelSmall);Text(value,color=Color.White,fontWeight=FontWeight.SemiBold)}}
