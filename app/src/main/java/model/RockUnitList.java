package model;

import java.util.ArrayList;

public class RockUnitList extends ArrayList<RockUnit> {
    private static final long serialVersionUID = 1L;
    private ArrayList<OnRockUnitListChangedListener> m_listChangedListeners;

    public RockUnitList(RockUnit[] arrrockUnit) {
        super(arrrockUnit.length);
        for (int i = 0; i < arrrockUnit.length; ++i) {
            super.add(arrrockUnit[i]);
        }
    }

    private void notifyListeners() {
        if (this.m_listChangedListeners != null) {
            if (this.m_listChangedListeners.isEmpty()) {
                return;
            }
            for (int i = -1 + this.m_listChangedListeners.size(); i >= 0; --i) {
                ((OnRockUnitListChangedListener)this.m_listChangedListeners.get(i)).onDataSetChanged();
            }
            return;
        }
    }

    public void add(int n, RockUnit rockUnit) {
        super.add(n, rockUnit);
    }

    public boolean add(RockUnit rockUnit) {
        super.add(rockUnit);
        return true;
    }

    public void addListener(OnRockUnitListChangedListener onRockUnitListChangedListener) {
        if (this.m_listChangedListeners == null) {
            this.m_listChangedListeners = new ArrayList();
        }
        if (!this.m_listChangedListeners.contains((Object)onRockUnitListChangedListener)) {
            this.m_listChangedListeners.add(onRockUnitListChangedListener);
        }
    }

    public void clear() {
        super.clear();
        this.notifyListeners();
    }

    public RockUnit remove(int n) {
        RockUnit rockUnit = (RockUnit)super.remove(n);
        return rockUnit;
    }

    public boolean remove(Object object) {
        boolean bl = super.remove(object);
        return bl;
    }

    public void removeListener(OnRockUnitListChangedListener onRockUnitListChangedListener) {
        if (this.m_listChangedListeners == null) {
            return;
        }
        this.m_listChangedListeners.remove((Object)onRockUnitListChangedListener);
    }

    public RockUnit set(int n, RockUnit rockUnit) {
        RockUnit rockUnit2 = (RockUnit)super.set(n, rockUnit);
        return rockUnit2;
    }

    public static interface OnRockUnitListChangedListener {
        public void onDataSetChanged();
    }

}

