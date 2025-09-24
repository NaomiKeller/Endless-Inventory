package com.kwwsyk.

/**page that items are splitted into segment by item classification 
*Segments 
*
*/
public class SegClassifyItemDisplay{

private List<Predicate<ItemStack>> subClassifies;
private List<ItemStack> view;
boolean includeRemainItems;//whether add not classfied items(all subclassify test ret false) to view
boolean keepClassifiedItemInNextSeg;//in the last subclassify an item test true, if keep the item can be added to the next subclassify test.

public SegClassifyItemDisplay(/*...*/, subClassifies,bool iRI,bool kCIiNS){}

public void -->view(){
list newView
list items = this.items
for(sc: subClassifies){
list seg = items.filter(sc).toList
if(!keep...) items-seg
int a = seg.size()
int b = seg.size()%meta.column
int c = meta.column-b
newView+seg+List.withCap(c, ItemStack.EMPTY)
}
if(include..&& !keep) newView + items
view = newView[0,meta.row*meta.column]
}
}
