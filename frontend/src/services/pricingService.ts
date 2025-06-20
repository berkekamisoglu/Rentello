import api from './api';

export interface PriceCalculationRequest {
  vehicleId: number;
  startDate: string;
  endDate: string;
}

export interface PricingBreakdown {
  basePrice: number;
  weekendSurcharge: number;
  seasonalAdjustment: number;
  discountAmount: number;
  taxAmount: number;
  totalPrice: number;
  totalDays: number;
  averageRate: number;
  breakdown: any[];
}

export interface PriceCalculationResponse {
  vehicleId: number;
  baseRate: number;
  startDate: string;
  endDate: string;
  totalPrice: number;
  breakdown: PricingBreakdown;
}

export interface PricingMultipliers {
  seasonal: {
    summer: string;
    winter: string;
    springFall: string;
  };
  special: {
    weekend: string;
    holiday: string;
    highDemand: string;
  };
}

class PricingService {
  /**
   * Calculate dynamic price for a vehicle and date range
   */
  async calculatePrice(request: PriceCalculationRequest): Promise<PriceCalculationResponse> {
    const response = await api.post('/pricing/calculate', request);
    return response.data;
  }

  /**
   * Get pricing breakdown for display
   */
  async getPricingBreakdown(vehicleId: number, startDate: string, endDate: string): Promise<PricingBreakdown> {
    const response = await api.get('/pricing/breakdown', {
      params: { vehicleId, startDate, endDate }
    });
    return response.data;
  }

  /**
   * Get pricing multipliers information
   */
  async getPricingMultipliers(): Promise<PricingMultipliers> {
    const response = await api.get('/pricing/multipliers');
    return response.data;
  }

  /**
   * Format price for display
   */
  formatPrice(price: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(price);
  }

  /**
   * Calculate days between two dates
   */
  calculateDays(startDate: string, endDate: string): number {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diffTime = Math.abs(end.getTime() - start.getTime());
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  /**
   * Check if date is weekend
   */
  isWeekend(date: string): boolean {
    const d = new Date(date);
    const day = d.getDay();
    return day === 0 || day === 6; // Sunday = 0, Saturday = 6
  }

  /**
   * Check if date is holiday (basic implementation)
   */
  isHoliday(date: string): boolean {
    const d = new Date(date);
    const month = d.getMonth() + 1; // getMonth() returns 0-11
    const day = d.getDate();
    
    // Basic holiday check (can be expanded)
    const holidays = [
      { month: 1, day: 1 },   // New Year's Day
      { month: 4, day: 23 },  // National Sovereignty Day
      { month: 5, day: 1 },   // Labor Day
      { month: 5, day: 19 },  // Commemoration of Atatürk
      { month: 7, day: 15 },  // Democracy Day
      { month: 8, day: 30 },  // Victory Day
      { month: 10, day: 29 }, // Republic Day
      { month: 12, day: 25 }, // Christmas
      { month: 12, day: 31 }  // New Year's Eve
    ];
    
    return holidays.some(holiday => holiday.month === month && holiday.day === day);
  }

  /**
   * Get season for a date
   */
  getSeason(date: string): 'summer' | 'winter' | 'spring' | 'fall' {
    const d = new Date(date);
    const month = d.getMonth() + 1;
    
    if (month >= 6 && month <= 8) return 'summer';
    if (month >= 12 || month <= 2) return 'winter';
    if (month >= 3 && month <= 5) return 'spring';
    return 'fall';
  }

  /**
   * Get pricing explanation for a date range
   */
  getPricingExplanation(breakdown: PricingBreakdown): string[] {
    const explanations: string[] = [];
    
    explanations.push(`Base price: ${this.formatPrice(breakdown.basePrice)}`);
    explanations.push(`Total days: ${breakdown.totalDays}`);
    
    if (breakdown.weekendSurcharge > 0) {
      explanations.push(`Weekend surcharge: +${this.formatPrice(breakdown.weekendSurcharge)}`);
    }
    
    if (breakdown.seasonalAdjustment !== 0) {
      const sign = breakdown.seasonalAdjustment > 0 ? '+' : '';
      explanations.push(`Seasonal adjustment: ${sign}${this.formatPrice(breakdown.seasonalAdjustment)}`);
    }
    
    if (breakdown.discountAmount > 0) {
      explanations.push(`Discount: -${this.formatPrice(breakdown.discountAmount)}`);
    }
    
    explanations.push(`Tax (18%): ${this.formatPrice(breakdown.taxAmount)}`);
    explanations.push(`Average rate: ${this.formatPrice(breakdown.averageRate)} per day`);
    explanations.push(`Total price: ${this.formatPrice(breakdown.totalPrice)}`);
    
    return explanations;
  }
}

export default new PricingService(); 